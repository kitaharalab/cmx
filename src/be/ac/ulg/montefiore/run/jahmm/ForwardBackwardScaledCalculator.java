/*
 * Copyright (c) 2004-2009, Jean-Marc François. All Rights Reserved.
 * Licensed under the New BSD license.  See the LICENSE file.
 */

package be.ac.ulg.montefiore.run.jahmm;

import java.util.*;


/**
 * This class can be used to compute the probability of a given observations
 * sequence for a given HMM.
 * <p>
 * This class implements the scaling method explained in <i>Rabiner</i> and 
 * <i>Juang</i>, thus the {@link #alphaElement(int,int) alphaElement} and
 * {@link #betaElement(int,int) betaElement} return the scaled alpha and
 * beta elements.  The <code>alpha</code> array must always be computed
 * because the scaling factors are computed together with it.
 * <p>
 * For more information on the scaling procedure, read <i>Rabiner</i> and 
 * <i>Juang</i>'s <i>Fundamentals of speech recognition</i> (Prentice Hall,
 * 1993).
 */
public class ForwardBackwardScaledCalculator
extends ForwardBackwardCalculator
{
	/*
	 * Warning, the semantic of the alpha and beta elements are changed;
	 * in this class, they have their value scaled.
	 */
	// Scaling factors
	private double[] ctFactors;
	private double lnProbability;
	
	
	/**
	 * Computes the probability of occurence of an observation sequence
	 * given a Hidden Markov Model.  The algorithms implemented use scaling
	 * to avoid underflows.
	 *
	 * @param hmm A Hidden Markov Model;
	 * @param oseq An observations sequence.
	 * @param flags How the computation should be done. See the
	 *              {@link ForwardBackwardCalculator.Computation}.
	 *              The alpha array is always computed.
	 */
	public <O extends Observation> 
	ForwardBackwardScaledCalculator(List<? extends O> oseq,
			Hmm<O> hmm, EnumSet<Computation> flags)
	{
		if (oseq.isEmpty())
			throw new IllegalArgumentException();
		
		ctFactors = new double[oseq.size()];
		Arrays.fill(ctFactors, 0.);
		
		computeAlpha(hmm, oseq);
		
		if (flags.contains(Computation.BETA))
			computeBeta(hmm, oseq);
		
		computeProbability(oseq, hmm, flags);
	}
	
	
	/**
	 * Computes the probability of occurence of an observation sequence
	 * given a Hidden Markov Model.  This computation computes the scaled
	 * <code>alpha</code> array as a side effect.
	 * @see #ForwardBackwardScaledCalculator(List, Hmm, EnumSet)
	 */
	public <O extends Observation>
	ForwardBackwardScaledCalculator(List<? extends O> oseq, Hmm<O> hmm)
	{
		this(oseq, hmm, EnumSet.of(Computation.ALPHA));
	}
	
	
	/* Computes the content of the scaled alpha array */
	protected <O extends Observation> void
	computeAlpha(Hmm<? super O> hmm, List<O> oseq)
	{	
		alpha = new double[oseq.size()][hmm.nbStates()];
		
		for (int i = 0; i < hmm.nbStates(); i++)
			computeAlphaInit(hmm, oseq.get(0), i);
		scale(ctFactors, alpha, 0);
		
		Iterator<? extends O> seqIterator = oseq.iterator();
		if (seqIterator.hasNext())
			seqIterator.next();
		
		for (int t = 1; t < oseq.size(); t++) {
			O observation = seqIterator.next();
			
			for (int i = 0; i < hmm.nbStates(); i++)
				computeAlphaStep(hmm, observation, t, i);
			scale(ctFactors, alpha, t);
		}
	}
	
	
	/* Computes the content of the scaled beta array.  The scaling factors are
	 those computed for alpha. */
	protected <O extends Observation> void 
	computeBeta(Hmm<? super O> hmm, List<O> oseq)
	{	
		beta = new double[oseq.size()][hmm.nbStates()];
		
		for (int i = 0; i < hmm.nbStates(); i++)
			beta[oseq.size()-1][i] = 1. / ctFactors[oseq.size()-1];
		
		for (int t = oseq.size() - 2; t >= 0; t--)
			for (int i = 0; i < hmm.nbStates(); i++) {
				computeBetaStep(hmm, oseq.get(t+1), t, i);
				beta[t][i] /= ctFactors[t];
			}
	}
	
	
	/* Normalize alpha[t] and put the normalization factor in ctFactors[t] */
	private void scale(double[] ctFactors, double[][] array, int t)
	{
		double[] table = array[t];
		double sum = 0.;
		
		for (int i = 0; i < table.length; i++)
			sum += table[i];
		
		ctFactors[t] = sum;
		for (int i = 0; i < table.length; i++) 
			table[i] /= sum;
	}
	
	
	private <O extends Observation> void
	computeProbability(List<O> oseq, Hmm<? super O> hmm, 
			EnumSet<Computation> flags)
	{	
		lnProbability = 0.;
		
		for (int t = 0; t < oseq.size(); t++)
			lnProbability += Math.log(ctFactors[t]);
		
		probability = Math.exp(lnProbability);
	}
	
	
	/**
	 * Return the neperian logarithm of the probability of the sequence that
	 * generated this object.
	 *
	 * @return The probability of the sequence of interest's neperian logarithm.
	 */
	public double lnProbability()
	{
		return lnProbability;
	}
}
