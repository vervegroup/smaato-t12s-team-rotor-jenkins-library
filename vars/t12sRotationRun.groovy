package io.t12s.automator.team.rotor.jenkins

Map<String, ?> call(final Map<String, String> config = [:]) {
    final Map<String, String> defaultConfig = [:]
    final Map<String, String> finalConfig = [:] << defaultConfig << config

    final def rotor = new T12sTeamRotor(this, finalConfig.teamId, finalConfig.teamSecret)

    final def rotationResult = rotor.rotate(finalConfig.rotationId)

    if ('verbose' in finalConfig.keySet() && finalConfig.verbose == 'true') {
        println rotationResult
    }

    return rotationResult
}