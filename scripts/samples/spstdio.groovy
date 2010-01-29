import jp.crestmuse.cmx.amusaj.sp.*
import jp.crestmuse.cmx.amusaj.filewrappers.*
import jp.crestmuse.cmx.math.*
import static jp.crestmuse.cmx.math.Operations.*
import static jp.crestmuse.cmx.math.Utils.*


def SPExecutor exec = new SPExecutor()
def reader = new SPStreamReader(System.in, "array", 2)
def writer = new SPStreamWriter(System.out)
def sum = new Sum()
exec.addSPModule(reader)
exec.addSPModule(sum)
exec.addSPModule(writer)
exec.connect(reader, 0, sum, 0)
exec.connect(sum, 0, writer, 0)
exec.start()

class Sum extends SPModule {
  void execute(Object[] src, TimeSeriesCompatible[] dest) {
    dest[0].add(create1dimDoubleArray(sum(src[0])))
  }
  Class[] getInputClasses() {
    [ DoubleArray.class ]
  }
  Class[] getOutputClasses() {
    [ DoubleArray.class ]
  }
}