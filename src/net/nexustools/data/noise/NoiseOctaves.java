/*
 * janxutils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 or any later version.
 * 
 * janxutils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with janxutils.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.nexustools.data.noise;

/**
 *
 * @author Luke
 */
public class NoiseOctaves {
    
    public final float res1D;
    public final float res2D;
    public final float lac;
    public final float persistance;
    public final NoiseGenerator noiseGen;
	
	public NoiseOctaves(float res1D, float res2D, float lac, float persistance, NoiseGenerator noiseGenerator) {
		noiseGen = noiseGenerator;
		this.res1D = res1D;
		this.res2D = res2D;
		this.lac = lac;
		this.persistance = persistance;
	}
	public NoiseOctaves(NoiseGenerator noiseGenerator) {
		this(8129f, 256f, 2f, 0.4f, noiseGenerator);
	}
    
    public float noise(int x, int y){ // x/y coordinate not float
        float interp = 0f;
        float amp = 1f;
        
        float xla = ((float)(x))/res2D;
        float yla = ((float)(y))/res2D;
        
        for(int oct = 0; oct < 32; oct++){
            interp += noiseGen.noise(xla, yla) * amp;
            xla *= lac;
            yla *= lac;
            amp *= persistance;
        }
        
        return interp;
    }
    
    public float getSpriteNoise(int x, int y){
        float ret = noiseGen.noise(x, y);
        ret = 1+ret/2;
        if(ret>1f)ret=1f;
        return ret;
    }
    
    public float noise(int v){
        float interp = 0f;
        float amp = 0.9f;
        
        float la = ((float)(v))/res1D;
        
        for(int oct = 0; oct < 32; oct++){
            interp += noiseGen.noise(la, 420.42f) * amp;
            la *= lac;
            amp *= persistance;
        }
        
        return interp;
        
    }
    
}
