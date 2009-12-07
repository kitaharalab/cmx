package jp.crestmuse.cmx.amusaj.commands;

public class ModuleConnection {
  ProducerConsumerCompatible inModule;
  ProducerConsumerCompatible outModule;
  int inCh;
  int outCh;

  public ModuleConnection(ProducerConsumerCompatible inModule, int inCh, 
                          ProducerConsumerCompatible outModule, int outCh) {
    this.inModule = inModule;
    this.inCh = inCh;
    this.outModule = outModule;
    this.outCh = outCh;
  }
}
