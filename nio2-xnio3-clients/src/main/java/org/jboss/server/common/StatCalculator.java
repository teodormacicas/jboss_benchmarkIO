/**
 * JBoss, Home of Professional Open Source. Copyright 2011, Red Hat, Inc., and
 * individual
 * contributors as indicated by the @author tags. See the copyright.txt file in
 * the distribution
 * for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation; either
 * version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this
 * software; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor,
 * Boston, MA 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.server.common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code StatCalculator}
 * <p/>
 * 
 * Created on May 3, 2012 at 11:42:28 AM
 * 
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 */
public class StatCalculator {

	/**
	 * Create a new instance of {@code StatCalculator}
	 */
	public StatCalculator() {
		super();
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		if (args.length < 1) {
			System.err.println("Usage: java " + StatCalculator.class.getName() + " path");
			System.exit(1);
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));

		String line = null;
		List<Double> values = new ArrayList<Double>();
		double avg = 0;

		while ((line = br.readLine()) != null) {
			if ("".equals(line.trim())) {
				continue;
			}

			String tab[] = line.split("\\s+");
			try {
				double value = Double.parseDouble(tab[2]);
				values.add(value);
				avg += value;

			} catch (NumberFormatException nfe) {

			}
		}
		br.close();
		avg /= values.size();
		double delta = delta(values, avg);
		System.out.println("AVG = " + avg + ", DELTA = " + delta + "\n");
	}

	/**
	 * 
	 * @param data
	 * @return
	 */
	private static double delta(List<Double> data, double avg) {
		double delta = 0;
		int n = data.size();
		for (double x : data) {
			delta += Math.pow(x - avg, 2);
		}

		delta = Math.sqrt(delta / (n * (n - 1)));

		return delta;
	}
}
