package ndwi

import geotrellis.raster.{DoubleConstantNoDataCellType, MultibandTile, Tile, isData}
import geotrellis.raster.io.geotiff.{GeoTiff, MultibandGeoTiff}
import geotrellis.raster.io.geotiff.reader.GeoTiffReader

import java.util

object App
{
  def main(args: Array[String]): Unit =
  {
    val path = "data/multiband.tif"

    val geotiff: MultibandGeoTiff = GeoTiffReader.readMultiband(path)

    val tile: MultibandTile = geotiff.tile
    val cols: Int = tile.cols
    val rows: Int = tile.rows
    val bands: Int = tile.bandCount

    println("cols: " + cols)
    println("rows: " + rows)
    println("bands: " + bands)

    // =============================== Codigo no funcional ===================================

    // Al intentar procesar mutiples datos de tipo double, el tiempo se extende demasiado
    // Por tal motivo solo se proceso 25 pixeles (5x5) y este demora alrededor de 5 segundos
    // Cuando intente procesar 100 pixeles (10x10) la PC se congelo de forma intermitente
    // Por lo tanto no es recomendable procesar una imagen satelital completa de esta forma

    /*

    // val data: Array[Double] = new Array[Double](cols*rows)
    val data: Array[Double] = new Array[Double](5*5)
    var index = 0

    for(i <- 0 until 5)
    {
      for(j <- 0 until 5)
      {
        val p0: Double = tile.band(0).get(j, i)
        val p1: Double = tile.band(1).get(j, i)

        val ndwi: Double = (p0 - p1) / (p0 + p1)

        if(ndwi > 0.4)  { data(index) = 1 }
        else  { data(index) = NODATA }
        index += 1
      }
    }

    val generated_tile = DoubleArrayTile(data, 5, 5)

    print(generated_tile.asciiDraw())

    */

    // =============================== Fin de Codigo no funcional ===================================

    // Por lo anteriormente expuesto se uso como referencia el codigo de este repositorio:
    // https://github.com/geotrellis/geotrellis-landsat-tutorial/blob/master/src/main/scala/tutorial/CreateNDWIPng.scala

    // La banda 1 es el green y la banda 4 es el infrarojo (cercano)
    val generated_tile: Tile = tile.convert(DoubleConstantNoDataCellType).combineDouble(1, 4) { (g, ir) =>
      if(isData(g) && isData(ir))
      {
        if((g - ir) / (g + ir) > 0.4) { 1 }
        else { Double.NaN }
      }
      else { Double.NaN }
    }

    val generated_raster = GeoTiff(generated_tile.convert(DoubleConstantNoDataCellType), geotiff.extent, geotiff.crs) // Creamos el raster
    generated_raster.write("ndwi.tiff")
  }
}
