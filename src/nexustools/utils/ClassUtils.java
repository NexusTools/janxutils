/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools.utils;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author katelyn
 */
public class ClassUtils {
	
	public static <M extends Map.Entry<? extends Class<?>, ?>> M bestMatch(Class<?> target, Set<M> entrySet) {
		M bestMatch = null;
		for(M entry : entrySet) {
			Class<?> test = entry.getKey();
			if(test.isAssignableFrom(target) && (bestMatch == null || bestMatch.getKey().isAssignableFrom(test)))
				bestMatch = entry;
		}
		return bestMatch;
	}
	
}
