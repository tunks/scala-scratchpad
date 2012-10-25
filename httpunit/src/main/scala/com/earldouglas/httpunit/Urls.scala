package com.earldouglas.httpunit

import java.net.HttpURLConnection
import java.net.URL

object Urls {
  implicit def stringToUrl(url: String): URL = new URL(url)
  def openConnection(url: URL): HttpURLConnection = url.openConnection().asInstanceOf[HttpURLConnection]
}