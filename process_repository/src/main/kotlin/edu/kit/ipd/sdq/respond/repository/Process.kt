package edu.kit.ipd.sdq.respond.repository

import javax.persistence.*
import javax.persistence.CascadeType.*

@Entity
@Table(name = "PROCESS")
class Process(@Column(name = "name") var name: String, @Column(name = "source", columnDefinition = "text") var source: String, @ManyToOne(cascade = [MERGE]) var plant: Plant) {
    constructor() : this("", "", Plant())

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0
}

@Entity
@Table(name = "PLANT")
class Plant(@Column(name = "path", unique = true) var path: String) {
    constructor() : this("/default")

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0
}

class ProcessDescriptor(val name: String, val id: Int)

val Process.processDescriptor: ProcessDescriptor
    get() = ProcessDescriptor(this.name, this.id)