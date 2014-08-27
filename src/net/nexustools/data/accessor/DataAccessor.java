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

package net.nexustools.data.accessor;

/**
 *
 * @author katelyn
 */
public interface DataAccessor<T, C, R> extends BaseAccessor {
	
	public static enum Reference {
		Strong,
		Soft,
		Weak
	}
	
	// Become Weak after an amount of time
	public static enum CacheLifetime {
		Blur(500), // .5 seconds
		Tiny(5000), // 5 seconds
		Short(30000), // 30 seconds
		Medium(60000 * 5), // 5 minutes
		Hour(60000 * 60), // 1 hour
		Day(60000 * 60 * 24), // 1 day
		Week(60000 * 60 * 24 * 7), // 7 days
		Month(60000 * 60 * 24 * 30), // 30 days
		
		 // Attempts to determine how long cache objects should live for
		Smart(-1);
		
		public final int life;
		CacheLifetime(int life) {
			this.life = life;
		}
	}
	
	public C type();
	public R refType();
	public boolean isTrue();
	public void clear();
	
}
