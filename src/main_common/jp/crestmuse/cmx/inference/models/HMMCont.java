package jp.crestmuse.cmx.inference.models;

public interface HMMCont extends HMM<Double> {
  enum Distr { GAUSS, GMM };
}
