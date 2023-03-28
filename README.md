# Jenkins Library for T12S-Automator Team Rotor

## Docs
- [DryRun](./vars/t12sRotationDryRun.md)
- [RotationRun](./vars/t12sRotationRun.md)
- [t12sRotationFetchResults](./vars/t12sRotationFetchResults.md)

## Example Jenkinsfile for DryRun

```groovy
@Library('t12s-team-rotor-jenkins-library') _

pipeline {
    agent { label 'Master || agent-multi-executors' }

    stages {
        stage('Weekly Presenter') {
            steps {
                script {
                    dryRunRotation('637aa3d1-123-45b8-abcd-b484e01e1234')
                }
            }
        }
        stage('Sprint Review Presenter') {
            steps {
                script {
                    dryRunRotation('2e70f78f-456-4c84-6789-5432195a0240')
                }
            }
        }
    }
}

private void dryRunRotation(final String rotationId) {
    final def result = t12sRotationDryRun([
            'teamId'    : 'your-team-id',
            'teamSecret': 'your-team-secret',
            'rotationId': rotationId
    ])

    final def resultAsString = result.toString()
    echo resultAsString
    final def chosenName = (result.memberOrder[0] as String).capitalize()
    final def rotationName = (result.rotationName as String).capitalize()

    final def message = """
                    ${rotationName} is :magic_wand: *${chosenName}* :party:.
                    Decision was made at `${result.createdAt}`.
                    _Fallback_: If *${chosenName}* can not do it, the next persons in line would be
                    *`${result.memberOrder.subList(1, result.memberOrder.size())}`*.
                    The order of the fallback persons could _change_ on next rotation run.
                    """.stripIndent()

    currentBuild.description = "${rotationName}: <b>${chosenName}</b>"
    slackSend(channel: 'test-slack-t12s-team-rotor-jenkins-library-test', color: '#00FF00', message: message)
}
```