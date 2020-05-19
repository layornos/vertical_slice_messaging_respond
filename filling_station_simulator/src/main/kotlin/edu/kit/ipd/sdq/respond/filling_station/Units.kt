package edu.kit.ipd.sdq.respond.filling_station

import java.time.Instant

typealias Millimeter = Int
typealias Gram = Int

val Instant.milli: Int
    get() = this.nano / 1000

