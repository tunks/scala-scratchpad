package com.earldouglas.slack

import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpOptions
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase
import org.apache.http.entity.InputStreamEntity
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.DefaultHttpClient

import java.io.InputStream

object `package` {

  sealed trait ResponseComponent[A]

  case object ResponseStatus extends ResponseComponent[Int]
  case object ResponseHeaders extends ResponseComponent[Map[String, String]]
  case class ResponseHeader(name: String) extends ResponseComponent[Option[String]]
  case object ResponseStream extends ResponseComponent[InputStream]
  case object ResponseContent extends ResponseComponent[String]

  val status = ResponseStatus
  val headers = ResponseHeaders
  val header = ResponseHeader
  val stream = ResponseStream
  val content = ResponseContent

  case class Response(status: Int, headers: Map[String, String], stream: InputStream) {

    def content = scala.io.Source.fromInputStream(stream).getLines().mkString("\n")
    def header(name: String) = 
      headers.get(name) match {
        case Some(v) => Some(v)
        case None    => headers.keys.filter(_.toLowerCase == name.toLowerCase).headOption.map(headers(_))
      }

    def apply[A](component: ResponseComponent[A]): A =
      component match {
        case ResponseStatus       => status
        case ResponseHeaders      => headers
        case ResponseHeader(name) => header(name)
        case ResponseStream       => stream
        case ResponseContent      => content
      }
  }

  class Requester(url: String, headers: Map[String, String] = Map.empty) {

    def header(name: String, value: String): Requester = new Requester(url, headers + (name -> value))
    
    def options: Response = request(new HttpOptions(url))
    def get: Response = request(new HttpGet(url))

    def post(stream: InputStream, length: Int): Response = postOrPut(stream, length, new HttpPost(url))
    def post(content: String): Response = post(new java.io.ByteArrayInputStream(content.getBytes), content.length)

    def put(stream: InputStream, length: Int): Response = postOrPut(stream, length, new HttpPut(url))
    def put(content: String): Response = put(new java.io.ByteArrayInputStream(content.getBytes), content.length)

    private def postOrPut(stream: InputStream, length: Int, req: HttpEntityEnclosingRequestBase): Response = {
      req.setEntity(new InputStreamEntity(stream, length))
      request(req)
    }

    private def request(httpRequest: HttpUriRequest) = {

      headers foreach { h => httpRequest.setHeader(h._1, h._2) }
 
      val httpClient = new DefaultHttpClient()
      val httpResponse = httpClient.execute(httpRequest)

      val resHeaders = httpResponse.getAllHeaders map { h => (h.getName, h.getValue) } toMap
      val stream = httpResponse.getEntity().getContent()

      Response(httpResponse.getStatusLine().getStatusCode(), resHeaders.toMap, stream)
    }

  }

  implicit def requester(url: String) = new Requester(url)
}
