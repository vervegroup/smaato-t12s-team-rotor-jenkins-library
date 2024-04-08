package io.t12s.automator.team.rotor.jenkins


import groovy.json.JsonSlurperClassic

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

class T12sTeamRotor implements Serializable {
  private static final System.Logger logger = System.getLogger(T12sTeamRotor.class.getName())

  @Serial
  static final long serialVersionUID = 1L
  private static final String BASE_URI_STRING = "https://t12s-rotor-api.smaatolabs.net/resources"

  private final String baseUri
  private final String teamId
  private final String teamSecret
  private final JsonSlurperClassic jsonParser
  private final HttpClient httpClient
  private final def step
  private final Duration timeoutDuration = Duration.ofSeconds(45)

  T12sTeamRotor(final String baseUri, final step, final String teamId, final String teamSecret) {
    logger.log(System.Logger.Level.DEBUG, "Creating T12sTeamRotor with baseUri: {0}, teamId: {1}", baseUri, teamId)
    this.baseUri = baseUri
    this.teamId = teamId
    this.teamSecret = teamSecret.trim().strip().replaceAll("\\s", '')
    this.step = step
    jsonParser = new JsonSlurperClassic()
    httpClient = HttpClient.newHttpClient()
  }

  T12sTeamRotor(final def step, final String teamId, final String teamSecret) {
    this(BASE_URI_STRING, step, teamId, teamSecret)
  }

  List<Map<String, ?>> fetchRotationRunResults(final String rotationId) {
    logger.log(System.Logger.Level.DEBUG, "fetchRotationRunResults T12sTeamRotor with teamId: {0}, rotationId: {1}", teamId, rotationId)
    final def rotationResultsUri = URI.create(baseUri +
      "/team/" + encode(teamId) + "/rotation/" + encode(rotationId) +
      "/runResults?secret=" + encode(teamSecret))

    final def rotationResultsRequest = HttpRequest.newBuilder(rotationResultsUri).
      timeout(timeoutDuration).
      header("accept", "application/json").
      GET().
      build()

    def uriAsString = rotationResultsUri.toASCIIString()
    logger.log(System.Logger.Level.DEBUG, 
      "fetchRotationRunResults T12sTeamRotor with teamId: {0}, rotationId: {1}, rotationResultsUri: {2}?secret=xyz",
      teamId, rotationId, uriAsString.substring(0, uriAsString.indexOf("secret=")))
    
    final rotationResultsResponse = httpClient.send(rotationResultsRequest, HttpResponse.BodyHandlers.ofString())
    if (rotationResultsResponse.statusCode() == 200) {
      logger.log(System.Logger.Level.DEBUG, "fetchRotationRunResults T12sTeamRotor with teamId: {0}, rotationId: {1}, rotationResultsResponse.statusCode: {2}", teamId, rotationId, rotationResultsResponse.statusCode())
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
    logger.log(System.Logger.Level.DEBUG, "internalRotate T12sTeamRotor with teamId: {0}, rotationId: {1} runMode: {2}", teamId, rotationId, runMode)
    final def rotateUri = URI.create(baseUri +
      "/team/" + encode(teamId) + "/rotation/" + encode(rotationId) +
      "/runResults?secret=" + encode(teamSecret) + "&saveMode=" + encode(runMode))

    def uriAsString = rotateUri.toASCIIString()
    logger.log(System.Logger.Level.DEBUG,
      "internalRotate T12sTeamRotor with teamId: {0}, rotationId: {1}, rotationResultsUri: {2}?secret=xyz",
      teamId, rotationId, uriAsString.substring(0, uriAsString.indexOf("secret=")))

    final def rotateRequest = HttpRequest.newBuilder(rotateUri).
      timeout(timeoutDuration).
      header("accept", "application/json").
      POST(HttpRequest.BodyPublishers.noBody()).
      build()

    return executeHttpRequest(rotateRequest)

  }

  private Map<String, ?> internalRotateOnce(final String rotationId, final String token) {
    logger.log(System.Logger.Level.DEBUG, "internalRotate T12sTeamRotor with teamId: {0}, rotationId: {1} token: {2}", teamId, rotationId, token)
    final def rotateUri = URI.create(baseUri +
      "/team/" + encode(teamId) + "/rotation/" + encode(rotationId) +
      "/runResults/" + encode(token) + "?secret=" + encode(teamSecret))


    def uriAsString = rotateUri.toASCIIString()
    logger.log(System.Logger.Level.DEBUG,
      "internalRotate T12sTeamRotor with teamId: {0}, rotationId: {1}, rotationResultsUri: {2}?secret=xyz",
      teamId, rotationId, uriAsString.substring(0, uriAsString.indexOf("secret=")))

    final def rotateRequest = HttpRequest.newBuilder(rotateUri).
      timeout(timeoutDuration).
      header("accept", "application/json").
      PUT(HttpRequest.BodyPublishers.noBody()).
      build()

    return executeHttpRequest(rotateRequest)

  }

  private Map<String, ?> executeHttpRequest(final HttpRequest rotateRequest) {
    def uriAsString = rotateRequest.uri().toASCIIString()
    logger.log(System.Logger.Level.DEBUG,
      "executeHttpRequest T12sTeamRotor with teamId: {0}, {2} {1}?secret=xyz",
      teamId, uriAsString.substring(0, uriAsString.indexOf("secret=")), rotateRequest.method())

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
