import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Utils.*

DoubleArray.mixin(Operations)
DoubleArray.mixin(Utils)
DoubleMatrix.mixin(Operations)
DoubleMatrix.mixin(Utils)

println("Test of matrix calculation")

x = createDoubleMatrix([[1.0, -1.0], [0.5, -1.5], 
    			[0.4, -0.8], [0.8, -0.9]])
println("x = \n${x.toString()}")
println("mean(x) = \n${x.meanrows().toString()}")
println("std(x) = \n${x.stdrows().toString()}")
println("cov(x) = \n${x.cov().toString()}")

println("Singular value decomposition: (U, S, V) = svd(x)")
(U, S, V) = x.svd()
println("U = \n${U.toString()}")
println("S = \n${S.toString()}")
println("V = \n${V.toString()}")
x2 = U * S * V.transposeX()
println("x = U * S * V\' = \n${x2.toString()}")
println("where \' means transposition.")  


println("Eigen decomposition: (V, D) = eig(y)")

y = createDoubleMatrix([[1.0, -5.0], [-5.0, 1.0]])
println("y = \n${y.toString()}");

(V, D) = y.eig();
println("V = \n${V.toString()}")
println("D = \n${D.toString()}")
y2 = V * D * V.transposeX()
println("y = V * D * V\' = \n${y.toString()}")

