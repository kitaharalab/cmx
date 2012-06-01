def S1 = ["import jp.crestmuse.cmx.amusaj.commands.*", 
	  "import jp.crestmuse.cmx.amusaj.filewrappers.*", 
	  "import jp.crestmuse.cmx.amusaj.sp.*", 
	  "import jp.crestmuse.cmx.commands.*", 
	  "import jp.crestmuse.cmx.filewrappers.*", 
	  "import jp.crestmuse.cmx.handlers.*", 
	  "import jp.crestmuse.cmx.inference.*", 
	  "import jp.crestmuse.cmx.math.*", 
	  "import jp.crestmuse.cmx.misc.*", 
	  "import jp.crestmuse.cmx.processing.*", 
	  "import jp.crestmuse.cmx.sound.*", 
	  "import jp.crestmuse.cmx.xml.processors.*", 
	  "import static jp.crestmuse.cmx.math.Operations.*", 
	  "import static jp.crestmuse.cmx.math.MathUtils.*", 
	  "", 
	  "DoubleArray.mixin(Operations)", 
	  "DoubleMatrix.mixin(Operations)", 
	  "ComplexArray.mixin(Operations)", 
	  "BooleanArray.mixin(Operations)"]
def S2 = ["class MyApp extends CMXApplet {"]
def S3 = ["}", 
       	  "(new MyApp()).runSketch()"]

if (args.length < 1) {
  System.err.println("Usage: cmxscript <script-file>")
  exit(1)
}

def codes1 = []
def codes2 = []

(new File(args[0])).eachLine {
  def line = it.trim()
  if (line.startsWith("package") || line.startsWith("import"))
    codes1.add(line)
  else
    codes2.add(line)
}
code = (S1+codes1+S2+codes2+S3).join("\n")

(new GroovyShell()).parse(code).run()

