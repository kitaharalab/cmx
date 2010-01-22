import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Utils.*

DoubleArray.mixin(Operations)
DoubleArray.mixin(Utils)
ComplexArray.mixin(Operations)
ComplexArray.mixin(Utils)

x = createComplexArray([-1.0, 1.0], [-2.0, 1.0])
y = createComplexArray([1.0, -1.0], [2.0, 3.0])
println("x = ${x.toString()}")
println("y = ${y.toString()}")
println("x + y = ${(x + y).toString()}")
println("x - y = ${(x - y).toString()}")
println("x * y = ${(x * y).toString()}")
println("x / y = ${(x / y).toString()}")

