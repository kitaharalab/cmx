#!/usr/bin/groovy

import jp.crestmuse.cmx.math.*
import static jp.crestmuse.cmx.math.Utils.*

DoubleArray.mixin(Operations)
DoubleArray.mixin(Utils)

x = createDoubleArray([1.0, 2.0])
y = createDoubleArray([3.0, 4.0])

println("${x.toString()} + ${y.toString()} = ${(x + y).toString()}")
println("${x.toString()} - ${y.toString()} = ${(x - y).toString()}")
println("${x.toString()} * 3 = ${(x * 3).toString()}")
println("${x.toString()} / 2 = ${(x / 2).toString()}")

