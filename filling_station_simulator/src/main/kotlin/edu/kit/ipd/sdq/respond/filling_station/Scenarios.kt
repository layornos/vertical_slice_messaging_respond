package edu.kit.ipd.sdq.respond.filling_station

import org.kodein.di.Kodein
import org.kodein.di.generic.*

val normalScenario = Kodein {
    bind<Slider>() with provider {
        SimulatedSlider()
    }
    bind<Scale>() with provider {
        SimulatedScale()
    }
    bind<Pump>() with provider {
        SimulatedPump()
    }
}
