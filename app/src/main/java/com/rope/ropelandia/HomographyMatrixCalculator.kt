package com.rope.ropelandia

import org.ejml.simple.SimpleMatrix

// https://math.stackexchange.com/questions/3509039/calculate-homography-with-and-without-svd

object HomographyMatrixCalculator {

    fun calculate(sourceRectangle: Rectangle, targetRectangle: Rectangle): HomographyMatrix {

        val pointsMatrix = SimpleMatrix(9, 9)

        feedMatrix(
            pointsMatrix,
            0,
            sourceRectangle.topLeft.x, sourceRectangle.topLeft.y,
            targetRectangle.topLeft.x, targetRectangle.topLeft.y
        )

        feedMatrix(
            pointsMatrix,
            2,
            sourceRectangle.topRight.x, sourceRectangle.topRight.y,
            targetRectangle.topRight.x, targetRectangle.topRight.y
        )

        feedMatrix(
            pointsMatrix,
            4,
            sourceRectangle.bottomRight.x, sourceRectangle.bottomRight.y,
            targetRectangle.bottomRight.x, targetRectangle.bottomRight.y
        )

        feedMatrix(
            pointsMatrix,
            6,
            sourceRectangle.bottomLeft.x, sourceRectangle.bottomLeft.y,
            targetRectangle.bottomLeft.x, targetRectangle.bottomLeft.y
        )

        val v = pointsMatrix.svd().v
        val homography = v.cols(8, 9)

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
        p: SimpleMatrix,
        row: Int,
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double
    ) {
        p.setRow(row, 0, -x1, -y1, -1.0, 0.0, 0.0, 0.0, x1 * x2, y1 * x2, x2)
        p.setRow(row + 1, 0, 0.0, 0.0, 0.0, -x1, -y1, -1.0, x1 * y2, y1 * y2, y2)
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