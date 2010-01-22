import jp.crestmuse.cmx.inference.*
import jp.crestmuse.cmx.math.*
import jp.crestmuse.cmx.amusaj.sp.*
import static jp.crestmuse.cmx.math.Utils.*
import be.ac.ulg.montefiore.run.jahmm.*
import be.ac.ulg.montefiore.run.jahmm.io.*

def hmmfilename = "sample.hmm"

def mr = new MusicRepresentation2(8, 8)
mr.addMusicLayerBasic("hmmstates", 2)
def reader = new File(hmmfilename).newReader()
def hmm2 = HmmReader.read(reader, new OpdfMultiGaussianReader())
def hmm = new HMM(hmm2)

def exec = new SPExecutor()
def reader2 = new SPStreamReader(System.in, "array", 1)
def hmmcalc = new HMMCalcModule(hmm)
hmmcalc.setMusicRepresentation(mr, "hmmstates")
def writer = new SPStreamWriter(System.out)
exec.addSPModule(reader2)
exec.addSPModule(hmmcalc)
exec.addSPModule(writer)
exec.connect(reader2, 0, hmmcalc, 0)
exec.connect(hmmcalc, 0, writer, 0)
exec.start()
