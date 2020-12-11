package edu.kit.ipd.sdq.respond.repository.tables

import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*
import javax.persistence.CascadeType.*

@Entity
@Table(name = "PROCESS", indexes = [Index(columnList = "uuid")])
data class Process(@Column(name = "name") var name: String, @Column(name = "source", columnDefinition = "text") var source: String, @ManyToOne(cascade = [MERGE]) var plant: Plant) {
    constructor() : this("", "", Plant())

    @GeneratedValue(generator = "UUID")
    @Column(name = "uuid")
    @GenericGenerator(
		name = "UUID",
		strategy = "org.hibernate.id.UUIDGenerator",
	)
    var uuid: UUID = UUID.randomUUID()

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0
}

@Entity
@Table(name = "PLANT")
data class Plant(@Column(name = "path", unique = true) var path: String) {
    constructor() : this("default")

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0
}

