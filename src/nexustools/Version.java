/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nexustools;

import nexustools.io.data.ClassStream;

/**
 *
 * @author katelyn
 * 
 * http://en.wikipedia.org/wiki/Software_versioning
 */
public class Version {
	
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
			return null;
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
	
}
