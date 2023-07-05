package io.t12s.automator.team.rotor.jenkins


import groovy.json.JsonSlurperClassic

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

class T12sTeamRotor implements Serializable {
  @Serial
  static final long serialVersionUID = 1L
  private static final String BASE_URI_STRING = "https://team-api.t12s-automator.app/resources"

  private final String baseUri
  private final String teamId
  private final String teamSecret
  private final JsonSlurperClassic jsonParser
  private final HttpClient httpClient
  private final def step
  private final Duration timeoutDuration = Duration.ofSeconds(45)

  T12sTeamRotor(final String baseUri, final step, final String teamId, final String teamSecret) {
    this.baseUri = baseUri
    this.teamId = teamId
    this.teamSecret = teamSecret
    this.step = step
    jsonParser = new JsonSlurperClassic()
    httpClient = HttpClient.newHttpClient()
  }

  T12sTeamRotor(final def step, final String teamId, final String teamSecret) {
    this(BASE_URI_STRING, step, teamId, teamSecret)
  }

  List<Map<String, ?>> fetchRotationRunResults(final String rotationId) {
    final def rotationResultsUri = URI.create(baseUri +
      "/team/" + encode(teamId) + "/rotation/" + encode(rotationId) +
      "/runResults?secret=" + encode(teamSecret))

    final def rotationResultsRequest = HttpRequest.newBuilder(rotationResultsUri).
      timeout(timeoutDuration).
      header("accept", "application/json").
      GET().
      build()

    final rotationResultsResponse = httpClient.send(rotationResultsRequest, HttpResponse.BodyHandlers.ofString())
    if (rotationResultsResponse.statusCode() == 200) {
      return jsonParser.parseText(rotationResultsResponse.body()) as List<Map<String, ?>>
    } else {
      step.echo("can not execute operation fetchRotationRunResults, statusCode: " + rotationResultsResponse.statusCode())
      step.echo(rotationResultsResponse.body())
      throw new IllegalStateException("can not execute operation fetchRotationRunResults: \n" + rotationResultsResponse.body())
    }
  }

  Map<String, ?> rotate(final String rotationId) {
    return internalRotate(rotationId, 'Save')
  }

  Map<String, ?> rotateDryRun(final String rotationId) {
    return internalRotate(rotationId, 'DryRun')
  }

  Map<String, ?> rotateOnce(final String rotationId, final String token) {
    return internalRotateOnce(rotationId, token)
  }

  private Map<String, ?> internalRotate(final String rotationId, final String runMode) {
    final def rotateUri = URI.create(baseUri +
      "/team/" + encode(teamId) + "/rotation/" + encode(rotationId) +
      "/runResults?secret=" + encode(teamSecret) + "&saveMode=" + encode(runMode))

    final def rotateRequest = HttpRequest.newBuilder(rotateUri).
      timeout(timeoutDuration).
      header("accept", "application/json").
      POST(HttpRequest.BodyPublishers.noBody()).
      build()

    return executeHttpRequest(rotateRequest)

  }

  private Map<String, ?> internalRotateOnce(final String rotationId, final String token) {
    final def rotateUri = URI.create(baseUri +
      "/team/" + encode(teamId) + "/rotation/" + encode(rotationId) +
      "/runResults/" + encode(token) + "?secret=" + encode(teamSecret))

    final def rotateRequest = HttpRequest.newBuilder(rotateUri).
      timeout(timeoutDuration).
      header("accept", "application/json").
      PUT(HttpRequest.BodyPublishers.noBody()).
      build()

    return executeHttpRequest(rotateRequest)

  }

  private Map<String, ?> executeHttpRequest(final HttpRequest rotateRequest) {
    final rotateResponse = httpClient.send(rotateRequest, HttpResponse.BodyHandlers.ofString())
    if (rotateResponse.statusCode() == 200) {
      return jsonParser.parseText(rotateResponse.body()) as Map<String, ?>
    } else {
      step.echo("can not execute operation internalRotate, statusCode: " + rotateResponse.statusCode())
      step.echo(rotateResponse.body())
      throw new IllegalStateException("can not execute operation internalRotate: \n" + rotateResponse.body())
    }
  }

  private static String encode(final String text) {
    return URLEncoder.encode(text, StandardCharsets.UTF_8)
  }
}
