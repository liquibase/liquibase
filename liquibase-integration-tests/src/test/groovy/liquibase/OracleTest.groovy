package liquibase

import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.logging.core.BufferedLogService
import liquibase.resource.SearchPathResourceAccessor
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout

import java.sql.ResultSet
import java.util.logging.Level

@LiquibaseIntegrationTest
class OracleTest extends Specification {

    @Shared
    private DatabaseTestSystem oracle = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("oracle") as DatabaseTestSystem
    static def lorem = """Lorem ipsum dolor sit amet, consectetur adipiscing elit. Ut sagittis feugiat velit tempus sollicitudin. Ut dapibus, tellus vel commodo commodo, est felis varius felis, eget pulvinar mauris risus eu lacus. Vestibulum quis orci vitae sem pulvinar semper. Fusce sit amet nulla enim. Maecenas faucibus vehicula pharetra. Pellentesque semper lacinia semper. Integer placerat ipsum a neque dapibus, vel eleifend ligula fermentum. Mauris hendrerit iaculis euismod. In venenatis ligula in velit venenatis, eu euismod lorem congue. Integer metus tortor, porttitor ut urna ut, maximus venenatis quam. Nam dictum odio libero, in feugiat lorem convallis vel. Phasellus enim lorem, ullamcorper eu pellentesque et, aliquam quis libero. Pellentesque bibendum felis vitae pulvinar tincidunt. Quisque et nulla convallis, varius lorem vel, iaculis justo. Nulla facilisi.Cras sagittis convallis risus et eleifend. Nunc ut pretium ipsum. Sed nec hendrerit sem. Morbi vestibulum eros vehicula erat mattis fermentum. Vivamus vel maximus felis. Nunc eget mauris in risus elementum ullamcorper. Phasellus semper consectetur mollis. Vivamus sit amet arcu vitae ligula molestie sollicitudin non sed ante. Suspendisse et rhoncus est.Nam vitae varius eros. Donec lorem dolor, varius eget fringilla quis, hendrerit quis sapien. Donec eget ipsum tempus, auctor augue nec, pharetra felis. Praesent maximus iaculis nunc, et iaculis magna pellentesque a. Nullam efficitur arcu dui, tempor luctus justo aliquam id. Morbi mattis, mi pellentesque fringilla cursus, urna mauris malesuada est, id sodales massa urna id massa. Aliquam elementum lobortis ligula, at sodales ante suscipit et. Aenean pellentesque finibus augue sit amet pulvinar. Duis nec nibh facilisis, fringilla libero ut, dignissim mauris. Donec orci arcu, vulputate tristique diam at, porttitor vulputate purus.Donec tincidunt eleifend arcu a pretium. Nunc nec turpis laoreet, luctus ex eu, pretium lacus. Mauris mi sapien, egestas a arcu sed, sodales imperdiet libero. Aliquam erat volutpat. Nulla eu est eleifend, elementum nisi non, dignissim arcu. In posuere eu sapien a facilisis. Nullam nec dui lectus. Praesent dictum augue sapien, vitae gravida justo mattis sed. Donec ullamcorper ullamcorper convallis. Integer dapibus maximus augue, sed luctus elit cursus et.Fusce ac augue nec orci faucibus bibendum. In vestibulum sodales porta. Praesent iaculis consectetur suscipit. Phasellus tempor est quis aliquet varius. Aliquam tincidunt vel urna ac fermentum. In mattis massa nibh, eu vestibulum turpis volutpat nec. Phasellus posuere leo ultrices purus fringilla fringilla. Integer augue ligula, varius quis elit a, sagittis egestas turpis. Curabitur velit metus, blandit vel nunc et, faucibus varius lorem. Pellentesque pharetra lacus justo, nec finibus ipsum tristique sit amet. Phasellus purus turpis, accumsan sit amet dolor quis, vehicula varius metus. Praesent sed nunc libero. Morbi faucibus iaculis nisi, at lobortis risus commodo et.Suspendisse potenti. Sed leo risus, venenatis at condimentum quis, facilisis pulvinar sem. Aenean varius libero nisl, et volutpat neque facilisis nec. Maecenas ac mi sit amet purus imperdiet condimentum. Vestibulum eget lacus odio. Nullam suscipit orci id ligula sollicitudin convallis. Morbi pulvinar, erat imperdiet auctor iaculis, massa nulla luctus erat, ac vehicula velit est eu dolor. Morbi lectus justo, vestibulum a purus nec, porttitor imperdiet magna. Nunc ut aliquet dui, at suscipit augue.Nam congue augue arcu, sit amet vehicula elit tincidunt in. Nunc sed purus lectus. Donec ultricies at diam eget consectetur. Nunc et odio nulla. Donec sed lacus diam. Vivamus at ultrices risus, vitae pretium lorem. Curabitur bibendum, nulla non bibendum ullamcorper, lectus ipsum viverra urna, a condimentum mauris arcu id felis.Proin mattis rutrum accumsan. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Integer gravida arcu vel commodo maximus. Interdum et malesuada fames ac ante ipsum primis in faucibus. Cras aliquet enim lacus, eu commodo erat lobortis ut. Vivamus bibendum mi ut commodo egestas. Maecenas blandit id risus vel suscipit. Donec pellentesque in lacus eget fringilla. Cras et tortor magna. In mattis eros nulla, vel porttitor massa congue quis. Phasellus quis elit erat. Vestibulum at sagittis dui.Curabitur eleifend consequat justo, nec luctus turpis accumsan vel. Nullam et ultrices libero. Etiam nibh ante, egestas sit amet iaculis nec, rutrum id risus. Integer vitae consectetur nibh, ut lacinia eros. Cras pellentesque euismod neque. Donec leo lacus, eleifend eget erat vitae, dignissim pulvinar est. Curabitur nulla nisl, faucibus feugiat pulvinar vitae, laoreet in dolor.Quisque leo erat, feugiat vel sodales eget, commodo eu lorem. Etiam sit amet laoreet ligula. Fusce lacus neque, porta in orci id, aliquam placerat magna. Mauris a semper mi. Aliquam augue sapien, tempor ut sollicitudin eu, consectetur ac urna. Nulla odio metus, fringilla in ultrices a, dapibus nec ligula. Nulla sed accumsan felis. Curabitur lacinia arcu molestie fringilla dictum. Fusce id neque nibh. Vestibulum vitae diam efficitur, hendrerit enim lacinia, auctor nibh.Sed pulvinar mattis velit, sit amet aliquam ante dictum et. Fusce tristique aliquet velit. Nunc varius quam non eros efficitur, eget maximus leo ultrices. Sed interdum velit dolor, non cursus lectus condimentum quis. Donec nec finibus est. Donec viverra et est eu ultrices. Nam tempor, odio a venenatis ornare, nulla arcu sodales felis, vitae sollicitudin ex ipsum sed risus. Aliquam convallis sed mauris id mattis. Praesent justo felis, semper vel lacus vestibulum, convallis accumsan orci. Sed a dictum dui, ut ultrices tortor. Ut sit amet dolor sit amet justo vulputate ornare.Sed justo libero, bibendum a nisl lacinia, porttitor accumsan dui. Vestibulum auctor dapibus gravida. Vestibulum rhoncus lacinia mauris vel fringilla. Aenean pellentesque feugiat risus, nec pellentesque orci facilisis sit amet. Vivamus malesuada, enim non imperdiet volutpat, tortor ipsum porttitor nisi, vitae dignissim augue lorem id felis. Sed eleifend diam magna, sit amet dictum orci suscipit quis. Sed ante turpis, gravida a diam id, efficitur pretium eros. Sed id vestibulum dolor, eget sollicitudin purus. Vivamus maximus, ipsum ultricies rhoncus pretium, quam risus malesuada massa, rutrum rhoncus purus lectus eget justo. Vestibulum elementum nunc venenatis, rutrum ligula at, consectetur ligula. Sed non arcu ut arcu lacinia blandit. Quisque quis eros et urna semper sodales. Curabitur et auctor ante, mollis hendrerit ex. Praesent eu aliquam ipsum, vel eleifend risus. Vestibulum facilisis sollicitudin lectus sit amet tristique. Vestibulum molestie nibh in maximus ultrices. Nam semper urna ut rhoncus tristique. Sed accumsan turpis quis."""

    def "Use loadData with really big clob from file"() {
        when:
        CommandUtil.runUpdate(oracle, "src/test/resources/changelogs/common/large-clob-data-load.xml")

        then:
        ResultSet resultSet = oracle.getConnection().prepareStatement("SELECT id, name FROM person").executeQuery()
        resultSet.next()
        resultSet.getString(2) == lorem
    }

    def "Able to use base64 encoded values directly from csv when loading blob column using loadData"() {
        when:
        CommandUtil.runUpdate(oracle, "src/test/resources/changelogs/common/inline-blob-data-load.xml")

        then:
        ResultSet resultSet = oracle.getConnection().prepareStatement("SELECT id, name FROM person").executeQuery()
        resultSet.next()
        new String(resultSet.getBytes(2)) == lorem
    }

    def "Use loadData with really big blob from file"() {
        when:
        CommandUtil.runUpdate(oracle, "src/test/resources/changelogs/common/large-blob-data-load.xml")

        then:
        ResultSet resultSet = oracle.getConnection().prepareStatement("SELECT id, name FROM person").executeQuery()
        resultSet.next()
        new String(resultSet.getBytes(2)) == lorem
    }

    def "Use loadData with invalid clob"() {
        when:
        BufferedLogService bufferLog = new BufferedLogService()

        Scope.child(Scope.Attr.logService.name(), bufferLog, () -> {
            CommandUtil.runUpdate(oracle, "src/test/resources/changelogs/common/invalid-clob-data-load.xml")
        })

        then:
        String logAsString = bufferLog.getLogAsString(Level.FINE)
        assert logAsString.contains("not found. Inserting the value as a string. See https://docs.liquibase.com for more information.")
    }

    @Timeout(value = 25)
    def "performance test"() {
        def changelogfile = "src/test/resources/changelogs/common/example-changelog.xml"
        when:
        CommandUtil.runUpdateCount(oracle, changelogfile, 1)

        oracle.executeSql("""
DECLARE
   v_counter NUMBER := 1;
BEGIN
   FOR i IN 1..40000 LOOP
      INSERT INTO DATABASECHANGELOG(ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE, CONTEXTS, LABELS, DEPLOYMENT_ID) 
      VALUES (TO_CHAR(v_counter), 'your.name', 'big.sql', TIMESTAMP '2024-04-24 16:12:31.507774', 100, 'EXECUTED', '9:22921d38f361c5f294e6a5092bb1b654', 'sql', 'example comment', NULL, 'DEV', 'example-context', 'example-label', '3975120367');
      v_counter := v_counter + 1;
   END LOOP;
END;
""")
        CommandUtil.runTag(oracle, "tag")

        CommandUtil.runUpdateCount(oracle, changelogfile, 1)

        CommandUtil.runReleaseLocks(oracle)

        CommandUtil.runHistory(oracle)

        CommandUtil.runRollback(new SearchPathResourceAccessor(".,target/test-classes"), oracle, changelogfile, "tag")

        then:
        noExceptionThrown()
    }

    def showRowsAffectedForDMLOnly() {
        when:
        BufferedLogService bufferLog = new BufferedLogService()

        Scope.child(Scope.Attr.logService.name(), bufferLog, () -> {
            CommandUtil.runUpdate(oracle, "src/test/resources/changelogs/common/rows-affected.xml")
        })

        then:
        String logAsString = bufferLog.getLogAsString(Level.FINE)
        assert logAsString.contains("0 row(s) affected")
        assert logAsString.contains("1 row(s) affected")
        assert ! logAsString.contains("-1 row(s) affected")
    }
}