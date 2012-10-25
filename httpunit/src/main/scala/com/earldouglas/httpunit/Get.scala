package com.earldouglas.httpunit

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

import Urls.openConnection

object Get {
  def apply(url: URL): Get = {
    new Get(url)
  }
}

class Get(url: URL) {
  def status(): Int = {
    val connection = openConnection(url)
    connection.setRequestMethod("GET")
    connection.setDoOutput(true)
    connection.setReadTimeout(10000)
    connection.connect()

    val is = connection.getInputStream()

    val buffer = new Array[Byte](1024)
    Stream.continually(is.read(buffer))
      .takeWhile(_ != -1)
      .foreach(_ => println(buffer))
    
    val status = connection.getResponseCode()
    connection.disconnect()
    status

  }
}