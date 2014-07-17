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

package nexustools.utils;

/**
 *
 * @author katelyn
 * 
 * http://en.wikipedia.org/wiki/Software_versioning
 */
public class Version implements Cloneable {
	
	/**
	 * The development stage
	 * http://en.wikipedia.org/wiki/Software_versioning#Designating_development_stage
	 */
	public static enum Stage {
		a("Alpha"),
		b("Beta"),
		rc("Release Candidate"),
		r("Release");
		
		private final String name;
		Stage(String name) {
			this.name = name;
		}
		
		public static Stage forOrdinal(int ordinal) {
			for(Stage stage : Stage.values())
				if(stage.ordinal() == ordinal)
					return stage;
			
			throw new UnsupportedOperationException("No Stage exists for ordinal: " + ordinal);
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public Version(byte major, byte minor, Stage stage, short revision) {
		if(stage == null)
			throw new NullPointerException();
		
		this.major = major;
		this.minor = minor;
		this.stage = stage;
		this.revision = revision;
	}
	
	public Version(byte major, byte minor, Stage stage) {
		this(major, minor, stage, (short)0);
	}
	
	public Version(byte major, byte minor) {
		this(major, minor, Stage.a, (short)0);
	}
	
	public final byte major;
	public final byte minor;
	public final Stage stage;
	public final short revision;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(major & 0xff);
		builder.append('.');
		builder.append(minor & 0xff);
		
		if(stage != Stage.r)
			builder.append(stage.name());
		else
			builder.append('.');
		
		builder.append(revision);
		
		return builder.toString();
	}
	
	@Override
	public Version clone() {
		return new Version(major, minor, stage, revision);
	}
	
}
