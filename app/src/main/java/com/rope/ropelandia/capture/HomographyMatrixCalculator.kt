package com.rope.ropelandia.capture

import com.rope.ropelandia.usecases.perspectiverectangle.domain.entities.PerspectiveRectangle
import org.ejml.simple.SimpleMatrix

// https://math.stackexchange.com/questions/3509039/calculate-homography-with-and-without-svd

object HomographyMatrixCalculator {

    fun calculate(perspectiveRectangle: PerspectiveRectangle, rectangle: Rectangle): HomographyMatrix {

        val pointsMatrix = SimpleMatrix(9, 9)

        feedMatrix(
            pointsMatrix,
            0,
            perspectiveRectangle.topLeft.x, perspectiveRectangle.topLeft.y,
            rectangle.left, rectangle.top
        )

        feedMatrix(
            pointsMatrix,
            2,
            perspectiveRectangle.topRight.x, perspectiveRectangle.topRight.y,
            rectangle.right, rectangle.top
        )

        feedMatrix(
            pointsMatrix,
            4,
            perspectiveRectangle.bottomRight.x, perspectiveRectangle.bottomRight.y,
            rectangle.right, rectangle.bottom
        )

        feedMatrix(
            pointsMatrix,
            6,
            perspectiveRectangle.bottomLeft.x, perspectiveRectangle.bottomLeft.y,
            rectangle.left, rectangle.bottom
        )

        val v = pointsMatrix.svd().v
        val homography = v.transpose().rows(8, 9)

        return HomographyMatrix(
            homography[0],
            homography[1],
            homography[2],
            homography[3],
            homography[4],
            homography[5],
            homography[6],
            homography[7],
            homography[8]
        )
    }

    private fun feedMatrix(
        matrix: SimpleMatrix,
        row: Int,
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double
    ) {
        matrix.setRow(row, 0, -x1, -y1, -1.0, 0.0, 0.0, 0.0, (x1 * x2), (y1 * x2), x2)
        matrix.setRow(row + 1, 0, 0.0, 0.0, 0.0, -x1, -y1, -1.0, (x1 * y2), (y1 * y2), y2)
    }

}

data class HomographyMatrix(
    val h11: Double,
    val h12: Double,
    val h13: Double,
    val h21: Double,
    val h22: Double,
    val h23: Double,
    val h31: Double,
    val h32: Double,
    val h33: Double
)

object PointPositionCalculator {
    fun calculatePoint(point: Point, homographyMatrix: HomographyMatrix): Point {

        val homography = SimpleMatrix(3, 3)
        homography[0, 0] = homographyMatrix.h11
        homography[0, 1] = homographyMatrix.h12
        homography[0, 2] = homographyMatrix.h13
        homography[1, 0] = homographyMatrix.h21
        homography[1, 1] = homographyMatrix.h22
        homography[1, 2] = homographyMatrix.h23
        homography[2, 0] = homographyMatrix.h31
        homography[2, 1] = homographyMatrix.h32
        homography[2, 2] = homographyMatrix.h33

        val pointVector = SimpleMatrix(3, 1)
        pointVector[0] = point.x
        pointVector[1] = point.y
        pointVector[2] = 1.0

        val result = homography.mult(pointVector)

        val scale = result[2]

        return Point(result[0] / scale, result[1] / scale)
    }
}