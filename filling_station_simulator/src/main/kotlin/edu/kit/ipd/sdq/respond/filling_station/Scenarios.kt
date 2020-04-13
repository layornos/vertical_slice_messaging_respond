package edu.kit.ipd.sdq.respond.filling_station

import org.kodein.di.Kodein
import org.kodein.di.generic.*

var numPumps = 0
val normalScenario = Kodein {
    bind<Slider>() with provider {
        SimulatedSlider(instance(), "position")
    }
    bind<Scale>() with provider {
        SimulatedScale(instance(), "weight")
    }
    bind<Pump>() with provider {
        SimulatedPump(instance(), "filling${numPumps++}")
    }
}
