import jp.crestmuse.cmx.math.*;
import static jp.crestmuse.cmx.math.Utils.*

/*****************************************************************
  This is a sample of various matrix calculation, e.g. 
  four arithmetic operaions, singular value decomposition, 
  and eigen decomposition.
 ****************************************************************/

DoubleArray.mixin(Operations)
DoubleArray.mixin(Utils)
DoubleMatrix.mixin(Operations)
DoubleMatrix.mixin(Utils)

println("Test of matrix calculation")

x = createDoubleMatrix([[1.0, -1.0], [0.5, -1.5], 
    			[0.4, -0.8], [0.8, -0.9]])
println("x = ${x.toString()}")
println("mean(x) = ${x.meanrows().toString()}")
println("std(x) = ${x.stdrows().toString()}")
println("cov(x) = ${x.cov().toString()}")
println("normalize(x) = ${x.normalize().toString()}")

println("Singular value decomposition: (U, S, V) = svd(x)")
(U, S, V) = x.svd()
println("U = ${U.toString()}")
println("S = ${S.toString()}")
println("V = ${V.toString()}")
x2 = U * S * V.transposeX()
println("x = U * S * V\' = ${x2.toString()}")
println("where \' means transposition.")  


println("Eigen decomposition: (V, D) = eig(y)")

y = createDoubleMatrix([[1.0, -5.0], [-5.0, 1.0]])
println("y = ${y.toString()}");

(V, D) = y.eig();
println("V = ${V.toString()}")
println("D = ${D.toString()}")
y2 = V * D * V.transposeX()
println("y = V * D * V\' = ${y.toString()}")

