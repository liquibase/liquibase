package liquibase.util

import liquibase.diff.output.changelog.ChangedObjectChangeGenerator
import liquibase.diff.output.changelog.MissingObjectChangeGenerator
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator
import liquibase.sdk.database.MockDatabase
import liquibase.structure.core.*
import spock.lang.Specification
import spock.lang.Unroll

class DependencyGraphTest extends Specification {

    @Unroll
    def "will order correctly for MissingObjectChangeGenerator"() {
        when:
        def graph = new DependencyGraph(MissingObjectChangeGenerator, new MockDatabase())

        then:
        graph.getOrderedOutputTypes(originalTypes) == expected

        where:
        originalTypes                | expected
        null                         | [Catalog, Schema, Sequence, StoredProcedure, Table, Column, Data, PrimaryKey, UniqueConstraint, ForeignKey, Index, View]
        []                           | []
        [Table]                      | [Table]
        [Table, Index]               | [Table, Index]
        [Table, Index, Column, View] | [Table, Column, Index, View]
        [Column, View, Index, Table] | [Table, Column, Index, View]
    }

    @Unroll
    def "will order correctly for ChangedObjectChangeGenerator"() {
        when:
        def graph = new DependencyGraph(ChangedObjectChangeGenerator, new MockDatabase())

        then:
        graph.getOrderedOutputTypes(originalTypes) == expected

        where:
        originalTypes                | expected
        null                         | [Catalog, Data, ForeignKey, Schema, Sequence, StoredProcedure, Table, Column, PrimaryKey, Index, UniqueConstraint, View]
        []                           | []
        [Table]                      | [Table]
        [Table, Index]               | [Table, Index]
        [Table, Index, Column, View] | [Table, Column, Index, View]
        [Column, View, Index, Table] | [Table, Column, Index, View]
    }

    @Unroll
    def "will order correctly for UnexpectedObjectChangeGenerator"() {
        when:
        def graph = new DependencyGraph(UnexpectedObjectChangeGenerator, new MockDatabase())

        then:
        graph.getOrderedOutputTypes(originalTypes) == expected

        where:
        originalTypes                | expected
        null                         | [Catalog, Data, ForeignKey, Schema, StoredProcedure, UniqueConstraint, View, Table, PrimaryKey, Column, Index, Sequence]
        []                           | []
        [Table]                      | [Table]
        [Table, Index]               | [Table, Index]
        [Table, Index, Column, View] | [View, Table, Column, Index]
        [Column, View, Index, Table] | [View, Table, Column, Index]
    }
}
