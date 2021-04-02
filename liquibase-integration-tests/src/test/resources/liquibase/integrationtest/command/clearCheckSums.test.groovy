package liquibase.integrationtest.command

import liquibase.integrationtest.setup.HistoryEntry

CommandTest.define {
    command = ["clearCheckSums"]

    run {
        arguments = [
                tag            : "version_2.0",
                "changeLogFile": "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        ]

        setup {
            history = [
                    new HistoryEntry(
                            id: "1",
                            author: "test",
                            path: "com/example/changelog.xml"
                    ),
                    new HistoryEntry(
                            id: "2",
                            author: "test",
                            path: "com/example/changelog.xml"
                    ),
            ]
        }

        expectedResults = [
                statusMessage: "Successfully executed clearCheckSums",
                statusCode   : 0
        ]

    }
}
