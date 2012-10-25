package com.earldouglas.httpunit

import org.scalatest.FunSuite

import com.earldouglas.httpunit.Urls.stringToUrl

class OkTests extends FunSuite {

  test("/ok returns status 200") {
    val status = Get("http://localhost:8080/ok").status()
    assert(status === 200)
  }
}
