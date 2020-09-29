package com.example.ropelandia

import org.ejml.data.DMatrix3
import org.ejml.data.DMatrix3x3
import org.ejml.simple.SimpleMatrix

object HomographyMatrixFactory {

    fun create(sourceRectangle: Rectangle, targetRectangle: Rectangle, point: Point): Point {

        val p = SimpleMatrix(9, 9)

        feedMatrix(
            p,
            0,
            sourceRectangle.topLeft.x, sourceRectangle.topLeft.y,
            targetRectangle.topLeft.x, targetRectangle.topLeft.y
        )

        feedMatrix(
            p,
            2,
            sourceRectangle.topRight.x, sourceRectangle.topRight.y,
            targetRectangle.topRight.x, targetRectangle.topRight.y
        )

        feedMatrix(
            p,
            4,
            sourceRectangle.bottomRight.x, sourceRectangle.bottomRight.y,
            targetRectangle.bottomRight.x, targetRectangle.bottomRight.y
        )

        feedMatrix(
            p,
            6,
            sourceRectangle.bottomLeft.x, sourceRectangle.bottomLeft.y,
            targetRectangle.bottomLeft.x, targetRectangle.bottomLeft.y
        )

        p.setRow(8, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0)

        val homogeneousVector = SimpleMatrix(9, 1)

        homogeneousVector.setColumn(
            0, 0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            1.0
        )

        val h = p.invert().mult(homogeneousVector)

        val matrix = SimpleMatrix(3, 3)
        matrix[0, 0] = h[0, 0]
        matrix[0, 1] = h[1, 0]
        matrix[0, 2] = h[2, 0]
        matrix[1, 0] = h[3, 0]
        matrix[1, 1] = h[4, 0]
        matrix[1, 2] = h[5, 0]
        matrix[2, 0] = h[6, 0]
        matrix[2, 1] = h[7, 0]
        matrix[2, 2] = h[8, 0]

        val pointVector = SimpleMatrix(3, 1)
        pointVector[0, 0] = point.x
        pointVector[1, 0] = point.y
        pointVector[2, 0] = 1.0


        val result = matrix.mult(pointVector)

        val d = result[2, 0]
        return Point(result[0,0]/ d, result[1,0]/ d)
//
//
//        return HomographyMatrix(
//            a = h[0, 0],
//            b = h[1, 0],
//            c = h[2, 0],
//            d = h[3, 0],
//            e = h[4, 0],
//            f = h[5, 0],
//            g = h[6, 0],
//            h = h[7, 0],
//            i = 1.0
//        )

    }

    private fun feedMatrix(
        p: SimpleMatrix,
        row: Int,
        x: Double,
        y: Double,
        xl: Double,
        yl: Double
    ) {
        p.setRow(row, 0, -x, -y, -1.0, 0.0, 0.0, 0.0, x * xl, y * xl, xl)
        p.setRow(row + 1, 0, 0.0, 0.0, 0.0, -x, -y, -1.0, x * yl, y * yl, yl)
    }

}

data class HomographyMatrix(
    val a: Double,
    val b: Double,
    val c: Double,
    val d: Double,
    val e: Double,
    val f: Double,
    val g: Double,
    val h: Double,
    val i: Double
)

object PointPositionCalculator {
    fun calcNewPoint(point: Point, homographyMatrix: HomographyMatrix): Point {

        val simpleMatrix = SimpleMatrix(3, 3)
        simpleMatrix[0, 0] = homographyMatrix.a
        simpleMatrix[0, 1] = homographyMatrix.b
        simpleMatrix[0, 2] = homographyMatrix.c
        simpleMatrix[1, 0] = homographyMatrix.d
        simpleMatrix[1, 1] = homographyMatrix.e
        simpleMatrix[1, 2] = homographyMatrix.f
        simpleMatrix[2, 0] = homographyMatrix.g
        simpleMatrix[2, 1] = homographyMatrix.h
        simpleMatrix[2, 2] = homographyMatrix.i

        val pointVector = SimpleMatrix(1, 3)
        pointVector[0, 0] = point.x
        pointVector[0, 1] = point.x
        pointVector[0, 2] = 1.0

        val result = simpleMatrix.mult(pointVector.transpose())

        return Point(result[0, 0], result[1, 0])
    }
}