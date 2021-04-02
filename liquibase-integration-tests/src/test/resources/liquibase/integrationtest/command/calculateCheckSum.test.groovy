package liquibase.integrationtest.command

import liquibase.command.core.CalculateCheckSumCommandStep

CommandTest.define {

    command = ["calculateCheckSum"]

    run {
        arguments = [
                (CalculateCheckSumCommandStep.CHANGESET_IDENTIFIER_ARG): "changelogs/hsqldb/complete/rollback.tag.changelog.xml::1::nvoxland",
                (CalculateCheckSumCommandStep.CHANGELOG_FILE_ARG)      : "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        ]

        expectedResults = [
                statusMessage: "Successfully executed calculateCheckSum",
                statusCode   : 0
        ]
    }
}
