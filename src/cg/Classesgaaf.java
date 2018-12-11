package cg;
import processing.core.*;
import oscP5.*;
import netP5.*;

import java.util.Locale;

public class Classesgaaf extends PApplet implements ResultListener {
	private static final long serialVersionUID = -3299301286172314518L;
	
	public OscP5 oscP5;
	public NetAddress myRemoteLocation;
	////////////////////////////////
	////verstelbare parameters// //
	///////////////////////////////
	//GUI parameters //
	public int FADE_COUNT = 0;                  // Aantal keren dan een bal kan stuiteren
	public boolean FADE_ENABLED = false;         // Aanzetten van de vervaagfunctie
	public float GRAVITY = 0;                    // Zwaartekracht 0-1 (0=uit)
	public float LUCHTWRIJVING = 1;         // Luchtwrijving 0-1 (0=strooplucht, 1=uit)
	public boolean GRAVITY_ENABLED = true;       // Zwaartekracht en luchtwrijving
	public boolean COLLISION_ENABLED = true;     // Stuiteren van ballen tegen elkaar
	public boolean ART = false;                  // ARTMODUS!!!! 
	public boolean WATER = false;                 // WATER!!!! 
	public float BALL_OPACITY = 10;              // de doorzichtigheid van de ballen
	public float EDGESHIFT = 0;              // bepaling van de stuiterrand
	public float WINDSNELHEID = 1.3f;
	public int BALLSHAPE = 0;
	public boolean FLOOR_ENABLED = false;
	public boolean CEILING_ENABLED = false;
	public boolean WALLS_ENABLED = false;
	public int STROKEWIDTH = 1;  
	public int STROKE_OPACITY = 255;
	public int BACKGROUNDR;
	public int BACKGROUNDG;
	public int BACKGROUNDB;
	public int BACKGROUND_OP;
	public int BALLRMIN;
	public int BALLRMAX;
	public int BALLGMIN;
	public int BALLGMAX;
	public int BALLBMIN;
	public int BALLBMAX;
	public int WATER_OP;
	public float MASSAINVLOED = 0;            // 0-100%
	public float DIAMTR; 
	public boolean RNDMDIAM = false;
	public int SHRINKAMNT;
	
	public static final String[] shapes = {"Ball", "Rippled Snowman", "Illu", "Aidsbal", "Mickey!"};
	public static Classesgaaf in = null;			// recept uitleg van max
	
	public int nBallen = 7;                                         /* Hoeveelheid ballen */
	public int breedte = 1920;                                       /* Breedte van de runscreen */
	public int hoogte = 1080;                                       /* Hoogte van de runscreen */
	public float diameter = DIAMTR;                                        /* Diameter van de ballen */
	public float snelheid = 1.05f;                                         /* Maximale Snelheid van de ballen */
	public float lolkekspasties = 0;                                   /* lolkekspasties! 0=uit */
	public float mFact = 0.001f;                                    // factor van massa (1-5)

	/////////////////////////////
	//// overige variabelen//  //
	/////////////////////////////
	public float wind;
	public boolean start;                 
	public float ballradius = diameter/2*EDGESHIFT;  
	public PVector lolkek;
	public SmartWindQueue swq = new SmartWindQueue(100,WINDSNELHEID);
	public PVector gravity = new PVector(0, GRAVITY);
	public Ball[] ballen;
	public boolean makeBalls;

	public OscMessage Pos = new OscMessage("/pos"); // = new OscMessage("/pos");
	public OscMessage FloorMes = new OscMessage("/bodem");
	public OscMessage WallMes = new OscMessage("/muur");
	public OscMessage CeilingMes = new OscMessage("/plafond");
	public OscMessage PosY = new OscMessage("/posy");
	public SimpleSettingsGUI ssg = new SimpleSettingsGUI();
	
	public static void main(String args[]) {
		PApplet.main(new String[] {"--present", "cg.Classesgaaf"});
	}
	
	//////////////
	////code// //
	//////////////
	public void setup() {
		in = this;

		/* start oscP5, listening for incoming messages at port 12000 */
		oscP5 = new OscP5(this,12000);
		myRemoteLocation = new NetAddress("127.0.0.1",12000);

		size(breedte, hoogte);
		Locale.setDefault(new Locale("en", "US"));

		//Send GUI Data//
		ssg.addData("Ball amount", 7);
		ssg.addData("Ball shape (0,1,2; 0=ball)", shapes);
		ssg.addData("Ball diameter", 100);
		ssg.addData("Ball speed (1-n)", 4.0);
		ssg.addData("Ball opacity (0-255)", 255.0);
		ssg.addData("Amount of bounces", 4);
		ssg.addData("Bounce limit toggle", false);
		ssg.addData("Floor bounce", true);
		ssg.addData("Ceiling bounce", true);
		ssg.addData("Wall bounce", true);
		ssg.addData("Gravity (0-1)", 0.3);
		ssg.addData("Air Resistance (0-1, default=0.9985)", 0.9985); 
		ssg.addData("Windspeed (0-n)", 1.3);
		ssg.addData("lolspasties (0-n; Bad windmode)", 0.0); 
		ssg.addData("Physics toggle", false);
		ssg.addData("Collision"  , true);
		ssg.addData("Shift edge (0-1)", 1.0);
		/* ssg.addData("Artmode", false); */
		ssg.addData("Stroke opacity (0-255)", 100);
		ssg.addData("Strokewidth", 0);
		ssg.addData("Background red (0-255)", 200);
		ssg.addData("Background green (0-255)", 200);
		ssg.addData("Background blue (0-255)", 200);    
		ssg.addData("Object color red min (0-255)", 0);
		ssg.addData("Object color red max (0-255)", 255);
		ssg.addData("Object color green min (0-255)", 0);
		ssg.addData("Object color green max (0-255)", 255);
		ssg.addData("Object color blue min (0-255)", 0);
		ssg.addData("Object color blue max (0-255)", 255);
		ssg.addData("Background opacity (0-255; 0=artmode, 255=opaque)", 120);
		ssg.addData("Seamode", false);
		ssg.addData("Sea opacity", 175);
		ssg.addData("Shrink toggle", false);
		ssg.addData("Amount of shrinks", 500);

		ssg.addPreset("Default", "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAABh3CAAAACAAAAAYdAAkQWlyIFJlc2lzdGFuY2UgKDAtMSwgZGVmYXVsdD0wLjk5ODUpc3IAD2phdmEubGFuZy5GbG9hdNrtyaLbPPDsAgABRgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHA/f52ydAAxQmFja2dyb3VuZCBvcGFjaXR5ICgwLTI1NTsgMD1hcnRtb2RlLCAyNTU9b3BhcXVlKXNyABFqYXZhLmxhbmcuSW50ZWdlchLioKT3gYc4AgABSQAFdmFsdWV4cQB+AAQAAAB4dAAXQmFja2dyb3VuZCBibHVlICgwLTI1NSlzcQB+AAcAAADIdAALU3Ryb2tld2lkdGhzcQB+AAcAAAAAdAAOUGh5c2ljcyB0b2dnbGVzcgARamF2YS5sYW5nLkJvb2xlYW7NIHKA1Zz67gIAAVoABXZhbHVleHAAdAAYQmFja2dyb3VuZCBncmVlbiAoMC0yNTUpc3EAfgAHAAAAyHQAFlN0cm9rZSBvcGFjaXR5ICgwLTI1NSlzcQB+AAcAAABkdAAPV2luZHNwZWVkICgwLW4pc3EAfgADP6ZmZnQADUdyYXZpdHkgKDAtMSlzcQB+AAM+mZmadAAWQmFja2dyb3VuZCByZWQgKDAtMjU1KXNxAH4ABwAAAMh0AB9sb2xzcGFzdGllcyAoMC1uOyBCYWQgd2luZG1vZGUpc3EAfgADAAAAAHQAE0JvdW5jZSBsaW1pdCB0b2dnbGVxAH4AD3QAC1dhbGwgYm91bmNlc3EAfgAOAXQAGEJhbGwgc2hhcGUgKDAsMTsgMD1iYWxsKXEAfgAMdAAJQ29sbGlzaW9ucQB+AB50AAtCYWxsIGFtb3VudHNxAH4ABwAAAAd0AAdTZWFtb2RlcQB+AA90AA1CYWxsIGRpYW1ldGVycQB+ABN0ABBTaGlmdCBlZGdlICgwLTEpc3EAfgADP4AAAHQAEUFtb3VudCBvZiBib3VuY2Vzc3EAfgAHAAAABHQADkNlaWxpbmcgYm91bmNlcQB+AB50ABRCYWxsIG9wYWNpdHkgKDAtMjU1KXNxAH4AA0N/AAB0ABBCYWxsIHNwZWVkICgxLW4pc3EAfgADQIAAAHQADEZsb29yIGJvdW5jZXEAfgAeeA==");
		ssg.addPreset("1", "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAABh3CAAAACAAAAAYdAAkQWlyIFJlc2lzdGFuY2UgKDAtMSwgZGVmYXVsdD0wLjk5ODUpc3IAD2phdmEubGFuZy5GbG9hdNrtyaLbPPDsAgABRgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHA/f52ydAAxQmFja2dyb3VuZCBvcGFjaXR5ICgwLTI1NTsgMD1hcnRtb2RlLCAyNTU9b3BhcXVlKXNyABFqYXZhLmxhbmcuSW50ZWdlchLioKT3gYc4AgABSQAFdmFsdWV4cQB+AAQAAAAJdAAXQmFja2dyb3VuZCBibHVlICgwLTI1NSlzcQB+AAcAAADIdAALU3Ryb2tld2lkdGhzcQB+AAcAAAADdAAOUGh5c2ljcyB0b2dnbGVzcgARamF2YS5sYW5nLkJvb2xlYW7NIHKA1Zz67gIAAVoABXZhbHVleHAAdAAYQmFja2dyb3VuZCBncmVlbiAoMC0yNTUpc3EAfgAHAAAAyHQAFlN0cm9rZSBvcGFjaXR5ICgwLTI1NSlzcQB+AAcAAADIdAAPV2luZHNwZWVkICgwLW4pc3EAfgADP6ZmZnQADUdyYXZpdHkgKDAtMSlzcQB+AAM+mZmadAAWQmFja2dyb3VuZCByZWQgKDAtMjU1KXNxAH4ABwAAAMh0AB9sb2xzcGFzdGllcyAoMC1uOyBCYWQgd2luZG1vZGUpc3EAfgADAAAAAHQAE0JvdW5jZSBsaW1pdCB0b2dnbGVxAH4AD3QAC1dhbGwgYm91bmNlc3EAfgAOAXQAGEJhbGwgc2hhcGUgKDAsMTsgMD1iYWxsKXNxAH4ABwAAAAB0AAlDb2xsaXNpb25xAH4AHnQAC0JhbGwgYW1vdW50cQB+AAx0AAdTZWFtb2RlcQB+AA90AA1CYWxsIGRpYW1ldGVyc3EAfgAHAAAA+nQAEFNoaWZ0IGVkZ2UgKDAtMSlzcQB+AAMAAAAAdAARQW1vdW50IG9mIGJvdW5jZXNzcQB+AAcAAAAEdAAOQ2VpbGluZyBib3VuY2VxAH4AHnQAFEJhbGwgb3BhY2l0eSAoMC0yNTUpc3EAfgADQ0gAAHQAEEJhbGwgc3BlZWQgKDEtbilzcQB+AANCIAAAdAAMRmxvb3IgYm91bmNlcQB+AB54");
		ssg.addPreset("2", "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAABh3CAAAACAAAAAZdAAkQWlyIFJlc2lzdGFuY2UgKDAtMSwgZGVmYXVsdD0wLjk5ODUpc3IAD2phdmEubGFuZy5GbG9hdNrtyaLbPPDsAgABRgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHA/gAAAdAAxQmFja2dyb3VuZCBvcGFjaXR5ICgwLTI1NTsgMD1hcnRtb2RlLCAyNTU9b3BhcXVlKXNyABFqYXZhLmxhbmcuSW50ZWdlchLioKT3gYc4AgABSQAFdmFsdWV4cQB+AAQAAAATdAAXQmFja2dyb3VuZCBibHVlICgwLTI1NSlzcQB+AAcAAADIdAALU3Ryb2tld2lkdGhzcQB+AAcAAAADdAAOUGh5c2ljcyB0b2dnbGVzcgARamF2YS5sYW5nLkJvb2xlYW7NIHKA1Zz67gIAAVoABXZhbHVleHABdAAaQmFsbCBzaGFwZSAoMCwxLDI7IDA9YmFsbClzcQB+AAcAAAACdAAYQmFja2dyb3VuZCBncmVlbiAoMC0yNTUpc3EAfgAHAAAAyHQAFlN0cm9rZSBvcGFjaXR5ICgwLTI1NSlzcQB+AAcAAADIdAAPV2luZHNwZWVkICgwLW4pc3EAfgADQsgAAHQADUdyYXZpdHkgKDAtMSlzcQB+AAM+mZmadAAWQmFja2dyb3VuZCByZWQgKDAtMjU1KXNxAH4ABwAAAMh0AB9sb2xzcGFzdGllcyAoMC1uOyBCYWQgd2luZG1vZGUpc3EAfgADQKAAAHQAE0JvdW5jZSBsaW1pdCB0b2dnbGVzcQB+AA4AdAALV2FsbCBib3VuY2VxAH4AH3QACUNvbGxpc2lvbnEAfgAfdAALQmFsbCBhbW91bnRzcQB+AAcAAAAydAAHU2VhbW9kZXEAfgAPdAANQmFsbCBkaWFtZXRlcnEAfgAjdAAQU2hpZnQgZWRnZSAoMC0xKXNxAH4AAwAAAAB0ABFBbW91bnQgb2YgYm91bmNlc3NxAH4ABwAAAAR0AA5DZWlsaW5nIGJvdW5jZXEAfgAPdAAUQmFsbCBvcGFjaXR5ICgwLTI1NSlzcQB+AANDSAAAdAALU2VhIG9wYWNpdHlzcQB+AAcAAAAAdAAQQmFsbCBzcGVlZCAoMS1uKXNxAH4AA0IgAAB0AAxGbG9vciBib3VuY2VxAH4AD3g=");
		ssg.addPreset("Wind & gravity", "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAABh3CAAAACAAAAAYdAAkQWlyIFJlc2lzdGFuY2UgKDAtMSwgZGVmYXVsdD0wLjk5ODUpc3IAD2phdmEubGFuZy5GbG9hdNrtyaLbPPDsAgABRgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHA/f52ydAAxQmFja2dyb3VuZCBvcGFjaXR5ICgwLTI1NTsgMD1hcnRtb2RlLCAyNTU9b3BhcXVlKXNyABFqYXZhLmxhbmcuSW50ZWdlchLioKT3gYc4AgABSQAFdmFsdWV4cQB+AAQAAACWdAAXQmFja2dyb3VuZCBibHVlICgwLTI1NSlzcQB+AAcAAADIdAALU3Ryb2tld2lkdGhzcQB+AAcAAAABdAAOUGh5c2ljcyB0b2dnbGVzcgARamF2YS5sYW5nLkJvb2xlYW7NIHKA1Zz67gIAAVoABXZhbHVleHABdAAYQmFja2dyb3VuZCBncmVlbiAoMC0yNTUpc3EAfgAHAAAAyHQAFlN0cm9rZSBvcGFjaXR5ICgwLTI1NSlzcQB+AAcAAADIdAAPV2luZHNwZWVkICgwLW4pc3EAfgADP6ZmZnQADUdyYXZpdHkgKDAtMSlzcQB+AAM+mZmadAAWQmFja2dyb3VuZCByZWQgKDAtMjU1KXNxAH4ABwAAAMh0AB9sb2xzcGFzdGllcyAoMC1uOyBCYWQgd2luZG1vZGUpc3EAfgADAAAAAHQAE0JvdW5jZSBsaW1pdCB0b2dnbGVzcQB+AA4AdAALV2FsbCBib3VuY2VxAH4AHXQAGEJhbGwgc2hhcGUgKDAsMTsgMD1iYWxsKXNxAH4ABwAAAAB0AAlDb2xsaXNpb25xAH4AHXQAC0JhbGwgYW1vdW50c3EAfgAHAAAAyHQAB1NlYW1vZGVxAH4AHXQADUJhbGwgZGlhbWV0ZXJzcQB+AAcAAAAKdAAQU2hpZnQgZWRnZSAoMC0xKXNxAH4AAwAAAAB0ABFBbW91bnQgb2YgYm91bmNlc3NxAH4ABwAAAAR0AA5DZWlsaW5nIGJvdW5jZXEAfgAddAAUQmFsbCBvcGFjaXR5ICgwLTI1NSlzcQB+AANDSAAAdAAQQmFsbCBzcGVlZCAoMS1uKXNxAH4AA0CAAAB0AAxGbG9vciBib3VuY2VxAH4AD3g=");
		ssg.addPreset("Patterns! 01 (click!)", "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAABh3CAAAACAAAAAYdAAkQWlyIFJlc2lzdGFuY2UgKDAtMSwgZGVmYXVsdD0wLjk5ODUpc3IAD2phdmEubGFuZy5GbG9hdNrtyaLbPPDsAgABRgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHA/f52ydAAxQmFja2dyb3VuZCBvcGFjaXR5ICgwLTI1NTsgMD1hcnRtb2RlLCAyNTU9b3BhcXVlKXNyABFqYXZhLmxhbmcuSW50ZWdlchLioKT3gYc4AgABSQAFdmFsdWV4cQB+AAQAAAAydAAXQmFja2dyb3VuZCBibHVlICgwLTI1NSlzcQB+AAcAAADIdAALU3Ryb2tld2lkdGhzcQB+AAcAAAADdAAOUGh5c2ljcyB0b2dnbGVzcgARamF2YS5sYW5nLkJvb2xlYW7NIHKA1Zz67gIAAVoABXZhbHVleHAAdAAYQmFja2dyb3VuZCBncmVlbiAoMC0yNTUpc3EAfgAHAAAAyHQAFlN0cm9rZSBvcGFjaXR5ICgwLTI1NSlzcQB+AAcAAADIdAAPV2luZHNwZWVkICgwLW4pc3EAfgADP6ZmZnQADUdyYXZpdHkgKDAtMSlzcQB+AAM+mZmadAAWQmFja2dyb3VuZCByZWQgKDAtMjU1KXNxAH4ABwAAAMh0AB9sb2xzcGFzdGllcyAoMC1uOyBCYWQgd2luZG1vZGUpc3EAfgADAAAAAHQAE0JvdW5jZSBsaW1pdCB0b2dnbGVxAH4AD3QAC1dhbGwgYm91bmNlcQB+AA90ABhCYWxsIHNoYXBlICgwLDE7IDA9YmFsbClzcQB+AAcAAAAAdAAJQ29sbGlzaW9uc3EAfgAOAXQAC0JhbGwgYW1vdW50c3EAfgAHAAAAAXQAB1NlYW1vZGVxAH4AD3QADUJhbGwgZGlhbWV0ZXJzcQB+AAcAAAD6dAAQU2hpZnQgZWRnZSAoMC0xKXNxAH4AAwAAAAB0ABFBbW91bnQgb2YgYm91bmNlc3NxAH4ABwAAAAR0AA5DZWlsaW5nIGJvdW5jZXEAfgAhdAAUQmFsbCBvcGFjaXR5ICgwLTI1NSlzcQB+AANDSAAAdAAQQmFsbCBzcGVlZCAoMS1uKXNxAH4AA0T6AAB0AAxGbG9vciBib3VuY2VxAH4AIXg=");
		ssg.addPreset("DNA", "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAADB3CAAAAEAAAAAgdAAkQWlyIFJlc2lzdGFuY2UgKDAtMSwgZGVmYXVsdD0wLjk5ODUpc3IAD2phdmEubGFuZy5GbG9hdNrtyaLbPPDsAgABRgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHA/f52ydAAaQmFsbCBzaGFwZSAoMCwxLDI7IDA9YmFsbClzcgARamF2YS5sYW5nLkludGVnZXIS4qCk94GHOAIAAUkABXZhbHVleHEAfgAEAAAAAnQAHU9iamVjdCBjb2xvciBibHVlIG1heCAoMC0yNTUpc3EAfgAHAAAA4XQAGEJhY2tncm91bmQgZ3JlZW4gKDAtMjU1KXNxAH4ABwAAAJZ0AA9XaW5kc3BlZWQgKDAtbilzcQB+AAM/pmZmdAANR3Jhdml0eSAoMC0xKXNxAH4AAz6ZmZp0AAtXYWxsIGJvdW5jZXNyABFqYXZhLmxhbmcuQm9vbGVhbs0gcoDVnPruAgABWgAFdmFsdWV4cAB0AB9sb2xzcGFzdGllcyAoMC1uOyBCYWQgd2luZG1vZGUpc3EAfgADAAAAAHQAFkJhY2tncm91bmQgcmVkICgwLTI1NSlzcQB+AAcAAACWdAAeT2JqZWN0IGNvbG9yIGdyZWVuIG1heCAoMC0yNTUpc3EAfgAHAAAAyHQAC0JhbGwgYW1vdW50c3EAfgAHAAACWHQAHE9iamVjdCBjb2xvciByZWQgbWluICgwLTI1NSlzcQB+AAcAAAAAdAARQW1vdW50IG9mIGJvdW5jZXNzcQB+AAcAAAAEdAALU2VhIG9wYWNpdHlzcQB+AAcAAAAAdAAeT2JqZWN0IGNvbG9yIGdyZWVuIG1pbiAoMC0yNTUpcQB+AB10AA9SYW5kb20gZGlhbWV0ZXJzcQB+ABIBdAAQQmFsbCBzcGVlZCAoMS1uKXNxAH4AA0EAAAB0AB1PYmplY3QgY29sb3IgYmx1ZSBtaW4gKDAtMjU1KXNxAH4ABwAAAMh0ADFCYWNrZ3JvdW5kIG9wYWNpdHkgKDAtMjU1OyAwPWFydG1vZGUsIDI1NT1vcGFxdWUpc3EAfgAHAAAAMnQAC1N0cm9rZXdpZHRoc3EAfgAHAAAAAXQAF0JhY2tncm91bmQgYmx1ZSAoMC0yNTUpc3EAfgAHAAAA/3QADlBoeXNpY3MgdG9nZ2xlcQB+ACR0ABZTdHJva2Ugb3BhY2l0eSAoMC0yNTUpc3EAfgAHAAAAyHQAE0JvdW5jZSBsaW1pdCB0b2dnbGVxAH4AE3QACUNvbGxpc2lvbnEAfgATdAAHU2VhbW9kZXEAfgAkdAANQmFsbCBkaWFtZXRlcnNxAH4ABwAAAA90ABBTaGlmdCBlZGdlICgwLTEpc3EAfgADAAAAAHQADkNlaWxpbmcgYm91bmNlcQB+ACR0ABRCYWxsIG9wYWNpdHkgKDAtMjU1KXNxAH4AA0NIAAB0ABxPYmplY3QgY29sb3IgcmVkIG1heCAoMC0yNTUpcQB+AB10AAxGbG9vciBib3VuY2VxAH4AJHg=");
		ssg.addPreset("Prachtige torrie", "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAABh3CAAAACAAAAAZdAAkQWlyIFJlc2lzdGFuY2UgKDAtMSwgZGVmYXVsdD0wLjk5ODUpc3IAD2phdmEubGFuZy5GbG9hdNrtyaLbPPDsAgABRgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHA/gAAAdAAxQmFja2dyb3VuZCBvcGFjaXR5ICgwLTI1NTsgMD1hcnRtb2RlLCAyNTU9b3BhcXVlKXNyABFqYXZhLmxhbmcuSW50ZWdlchLioKT3gYc4AgABSQAFdmFsdWV4cQB+AAQAAAAAdAAXQmFja2dyb3VuZCBibHVlICgwLTI1NSlzcQB+AAcAAADIdAALU3Ryb2tld2lkdGhzcQB+AAcAAAADdAAOUGh5c2ljcyB0b2dnbGVzcgARamF2YS5sYW5nLkJvb2xlYW7NIHKA1Zz67gIAAVoABXZhbHVleHABdAAaQmFsbCBzaGFwZSAoMCwxLDI7IDA9YmFsbClxAH4ADHQAGEJhY2tncm91bmQgZ3JlZW4gKDAtMjU1KXNxAH4ABwAAAMh0ABZTdHJva2Ugb3BhY2l0eSAoMC0yNTUpc3EAfgAHAAAAZHQAD1dpbmRzcGVlZCAoMC1uKXNxAH4AAwAAAAB0AA1HcmF2aXR5ICgwLTEpc3EAfgADPpmZmnQAFkJhY2tncm91bmQgcmVkICgwLTI1NSlzcQB+AAcAAADIdAAfbG9sc3Bhc3RpZXMgKDAtbjsgQmFkIHdpbmRtb2RlKXNxAH4AAwAAAAB0ABNCb3VuY2UgbGltaXQgdG9nZ2xlc3EAfgAOAHQAC1dhbGwgYm91bmNlcQB+AB50AAlDb2xsaXNpb25xAH4AD3QAC0JhbGwgYW1vdW50c3EAfgAHAAAAAXQAB1NlYW1vZGVxAH4AD3QADUJhbGwgZGlhbWV0ZXJzcQB+AAcAAABGdAAQU2hpZnQgZWRnZSAoMC0xKXNxAH4AAwAAAAB0ABFBbW91bnQgb2YgYm91bmNlc3NxAH4ABwAAAAR0AA5DZWlsaW5nIGJvdW5jZXEAfgAPdAAUQmFsbCBvcGFjaXR5ICgwLTI1NSlzcQB+AANDfwAAdAALU2VhIG9wYWNpdHlxAH4ACHQAEEJhbGwgc3BlZWQgKDEtbilzcQB+AANAQAAAdAAMRmxvb3IgYm91bmNlcQB+AA94");
		ssg.addPreset("Prachtige torrie 2.0", "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAABh3CAAAACAAAAAZdAAkQWlyIFJlc2lzdGFuY2UgKDAtMSwgZGVmYXVsdD0wLjk5ODUpc3IAD2phdmEubGFuZy5GbG9hdNrtyaLbPPDsAgABRgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHA/gAAAdAAxQmFja2dyb3VuZCBvcGFjaXR5ICgwLTI1NTsgMD1hcnRtb2RlLCAyNTU9b3BhcXVlKXNyABFqYXZhLmxhbmcuSW50ZWdlchLioKT3gYc4AgABSQAFdmFsdWV4cQB+AAQAAAAAdAAXQmFja2dyb3VuZCBibHVlICgwLTI1NSlzcQB+AAcAAADIdAALU3Ryb2tld2lkdGhzcQB+AAcAAAADdAAOUGh5c2ljcyB0b2dnbGVzcgARamF2YS5sYW5nLkJvb2xlYW7NIHKA1Zz67gIAAVoABXZhbHVleHABdAAaQmFsbCBzaGFwZSAoMCwxLDI7IDA9YmFsbClzcQB+AAcAAAAEdAAYQmFja2dyb3VuZCBncmVlbiAoMC0yNTUpc3EAfgAHAAAAyHQAFlN0cm9rZSBvcGFjaXR5ICgwLTI1NSlzcQB+AAcAAABkdAAPV2luZHNwZWVkICgwLW4pc3EAfgADAAAAAHQADUdyYXZpdHkgKDAtMSlzcQB+AAM+mZmadAAWQmFja2dyb3VuZCByZWQgKDAtMjU1KXNxAH4ABwAAAMh0AB9sb2xzcGFzdGllcyAoMC1uOyBCYWQgd2luZG1vZGUpc3EAfgADAAAAAHQAE0JvdW5jZSBsaW1pdCB0b2dnbGVzcQB+AA4AdAALV2FsbCBib3VuY2VxAH4AH3QACUNvbGxpc2lvbnEAfgAPdAALQmFsbCBhbW91bnRzcQB+AAcAAAADdAAHU2VhbW9kZXEAfgAPdAANQmFsbCBkaWFtZXRlcnNxAH4ABwAAAEZ0ABBTaGlmdCBlZGdlICgwLTEpc3EAfgADAAAAAHQAEUFtb3VudCBvZiBib3VuY2Vzc3EAfgAHAAAABHQADkNlaWxpbmcgYm91bmNlcQB+AA90ABRCYWxsIG9wYWNpdHkgKDAtMjU1KXNxAH4AA0N/AAB0AAtTZWEgb3BhY2l0eXEAfgAIdAAQQmFsbCBzcGVlZCAoMS1uKXNxAH4AA0BAAAB0AAxGbG9vciBib3VuY2VxAH4AD3g=");
		ssg.addPreset("DIKKE ASS", "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAABh3CAAAACAAAAAZdAAkQWlyIFJlc2lzdGFuY2UgKDAtMSwgZGVmYXVsdD0wLjk5ODUpc3IAD2phdmEubGFuZy5GbG9hdNrtyaLbPPDsAgABRgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHA/f52ydAAxQmFja2dyb3VuZCBvcGFjaXR5ICgwLTI1NTsgMD1hcnRtb2RlLCAyNTU9b3BhcXVlKXNyABFqYXZhLmxhbmcuSW50ZWdlchLioKT3gYc4AgABSQAFdmFsdWV4cQB+AAQAAAAydAAXQmFja2dyb3VuZCBibHVlICgwLTI1NSlzcQB+AAcAAADIdAALU3Ryb2tld2lkdGhzcQB+AAcAAAAUdAAOUGh5c2ljcyB0b2dnbGVzcgARamF2YS5sYW5nLkJvb2xlYW7NIHKA1Zz67gIAAVoABXZhbHVleHAAdAAaQmFsbCBzaGFwZSAoMCwxLDI7IDA9YmFsbClzcQB+AAcAAAACdAAYQmFja2dyb3VuZCBncmVlbiAoMC0yNTUpc3EAfgAHAAAAyHQAFlN0cm9rZSBvcGFjaXR5ICgwLTI1NSlzcQB+AAcAAAD/dAAPV2luZHNwZWVkICgwLW4pc3EAfgADP6ZmZnQADUdyYXZpdHkgKDAtMSlzcQB+AAM+mZmadAAWQmFja2dyb3VuZCByZWQgKDAtMjU1KXNxAH4ABwAAAMh0AB9sb2xzcGFzdGllcyAoMC1uOyBCYWQgd2luZG1vZGUpc3EAfgADAAAAAHQAE0JvdW5jZSBsaW1pdCB0b2dnbGVxAH4AD3QAC1dhbGwgYm91bmNlc3EAfgAOAXQACUNvbGxpc2lvbnEAfgAPdAALQmFsbCBhbW91bnRzcQB+AAcAAAABdAAHU2VhbW9kZXEAfgAPdAANQmFsbCBkaWFtZXRlcnNxAH4ABwAAAMh0ABBTaGlmdCBlZGdlICgwLTEpc3EAfgADAAAAAHQAEUFtb3VudCBvZiBib3VuY2Vzc3EAfgAHAAAABHQADkNlaWxpbmcgYm91bmNlcQB+ACB0ABRCYWxsIG9wYWNpdHkgKDAtMjU1KXNxAH4AA0N/AAB0AAtTZWEgb3BhY2l0eXNxAH4ABwAAAAB0ABBCYWxsIHNwZWVkICgxLW4pc3EAfgADQsgAAHQADEZsb29yIGJvdW5jZXEAfgAgeA==");
		ssg.addPreset("xXx Zhmall zballz xXx", "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAADB3CAAAAEAAAAAadAAkQWlyIFJlc2lzdGFuY2UgKDAtMSwgZGVmYXVsdD0wLjk5ODUpc3IAD2phdmEubGFuZy5GbG9hdNrtyaLbPPDsAgABRgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHA/f52ydAAaQmFsbCBzaGFwZSAoMCwxLDI7IDA9YmFsbClzcgARamF2YS5sYW5nLkludGVnZXIS4qCk94GHOAIAAUkABXZhbHVleHEAfgAEAAAAAHQAGEJhY2tncm91bmQgZ3JlZW4gKDAtMjU1KXNxAH4ABwAAAMh0AA9XaW5kc3BlZWQgKDAtbilzcQB+AAM/pmZmdAANR3Jhdml0eSAoMC0xKXNxAH4AAz6ZmZp0AAtXYWxsIGJvdW5jZXNyABFqYXZhLmxhbmcuQm9vbGVhbs0gcoDVnPruAgABWgAFdmFsdWV4cAF0AB9sb2xzcGFzdGllcyAoMC1uOyBCYWQgd2luZG1vZGUpc3EAfgADAAAAAHQAFkJhY2tncm91bmQgcmVkICgwLTI1NSlzcQB+AAcAAADIdAALQmFsbCBhbW91bnRzcQB+AAcAAAABdAARQW1vdW50IG9mIGJvdW5jZXNzcQB+AAcAAAAEdAALU2VhIG9wYWNpdHlzcQB+AAcAAACvdAAPUmFuZG9tIGRpYW1ldGVycQB+ABF0ABBCYWxsIHNwZWVkICgxLW4pc3EAfgADQsgAAHQAC1N0cm9rZXdpZHRocQB+AAh0ABdCYWNrZ3JvdW5kIGJsdWUgKDAtMjU1KXNxAH4ABwAAAMh0ADFCYWNrZ3JvdW5kIG9wYWNpdHkgKDAtMjU1OyAwPWFydG1vZGUsIDI1NT1vcGFxdWUpcQB+AAh0AA5QaHlzaWNzIHRvZ2dsZXNxAH4AEAB0ABZTdHJva2Ugb3BhY2l0eSAoMC0yNTUpc3EAfgAHAAAAZHQAE0JvdW5jZSBsaW1pdCB0b2dnbGVxAH4AJHQACUNvbGxpc2lvbnEAfgAkdAAHU2VhbW9kZXEAfgAkdAANQmFsbCBkaWFtZXRlcnNxAH4ABwAAAfR0ABBTaGlmdCBlZGdlICgwLTEpc3EAfgADAAAAAHQADkNlaWxpbmcgYm91bmNlcQB+ABF0ABRCYWxsIG9wYWNpdHkgKDAtMjU1KXNxAH4AA0N/AAB0AAxGbG9vciBib3VuY2VxAH4AEXg=");
		ssg.addPreset("DMB Swag", "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAADB3CAAAAEAAAAAadAAkQWlyIFJlc2lzdGFuY2UgKDAtMSwgZGVmYXVsdD0wLjk5ODUpc3IAD2phdmEubGFuZy5GbG9hdNrtyaLbPPDsAgABRgAFdmFsdWV4cgAQamF2YS5sYW5nLk51bWJlcoaslR0LlOCLAgAAeHA/f52ydAAaQmFsbCBzaGFwZSAoMCwxLDI7IDA9YmFsbClzcgARamF2YS5sYW5nLkludGVnZXIS4qCk94GHOAIAAUkABXZhbHVleHEAfgAEAAAAAHQAGEJhY2tncm91bmQgZ3JlZW4gKDAtMjU1KXNxAH4ABwAAAMh0AA9XaW5kc3BlZWQgKDAtbilzcQB+AAM/pmZmdAANR3Jhdml0eSAoMC0xKXNxAH4AAz6ZmZp0AAtXYWxsIGJvdW5jZXNyABFqYXZhLmxhbmcuQm9vbGVhbs0gcoDVnPruAgABWgAFdmFsdWV4cAF0AB9sb2xzcGFzdGllcyAoMC1uOyBCYWQgd2luZG1vZGUpc3EAfgADAAAAAHQAFkJhY2tncm91bmQgcmVkICgwLTI1NSlzcQB+AAcAAADIdAALQmFsbCBhbW91bnRzcQB+AAcAAAABdAARQW1vdW50IG9mIGJvdW5jZXNzcQB+AAcAAAAEdAALU2VhIG9wYWNpdHlzcQB+AAcAAACvdAAPUmFuZG9tIGRpYW1ldGVyc3EAfgAQAHQAEEJhbGwgc3BlZWQgKDEtbilzcQB+AANBoAAAdAALU3Ryb2tld2lkdGhzcQB+AAcAAAAAdAAXQmFja2dyb3VuZCBibHVlICgwLTI1NSlzcQB+AAcAAADIdAAxQmFja2dyb3VuZCBvcGFjaXR5ICgwLTI1NTsgMD1hcnRtb2RlLCAyNTU9b3BhcXVlKXNxAH4ABwAAAHh0AA5QaHlzaWNzIHRvZ2dsZXEAfgAddAAWU3Ryb2tlIG9wYWNpdHkgKDAtMjU1KXNxAH4ABwAAAGR0ABNCb3VuY2UgbGltaXQgdG9nZ2xlcQB+AB10AAlDb2xsaXNpb25xAH4AHXQAB1NlYW1vZGVxAH4AHXQADUJhbGwgZGlhbWV0ZXJzcQB+AAcAAAEsdAAQU2hpZnQgZWRnZSAoMC0xKXNxAH4AAz+AAAB0AA5DZWlsaW5nIGJvdW5jZXEAfgARdAAUQmFsbCBvcGFjaXR5ICgwLTI1NSlzcQB+AANDfwAAdAAMRmxvb3IgYm91bmNlcQB+ABF4");
		ssg.addPreset("Te Mooi", "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAADB3CAAAAEAAAAAhdAAXQmFja2dyb3VuZCBibHVlICgwLTI1NSlzcgARamF2YS5sYW5nLkludGVnZXIS4qCk94GHOAIAAUkABXZhbHVleHIAEGphdmEubGFuZy5OdW1iZXKGrJUdC5TgiwIAAHhwAAAAyHQAGkJhbGwgc2hhcGUgKDAsMSwyOyAwPWJhbGwpc3EAfgADAAAAA3QAC1dhbGwgYm91bmNlc3IAEWphdmEubGFuZy5Cb29sZWFuzSBygNWc+u4CAAFaAAV2YWx1ZXhwAXQADVNocmluayB0b2dnbGVzcQB+AAkAdAAdT2JqZWN0IGNvbG9yIGJsdWUgbWF4ICgwLTI1NSlzcQB+AAMAAAD/dAAkQWlyIFJlc2lzdGFuY2UgKDAtMSwgZGVmYXVsdD0wLjk5ODUpc3IAD2phdmEubGFuZy5GbG9hdNrtyaLbPPDsAgABRgAFdmFsdWV4cQB+AAQ/gAAAdAATQm91bmNlIGxpbWl0IHRvZ2dsZXEAfgAKdAANR3Jhdml0eSAoMC0xKXNxAH4AED9mZmZ0AAtCYWxsIGFtb3VudHNxAH4AAwAAAAF0ABhCYWNrZ3JvdW5kIGdyZWVuICgwLTI1NSlzcQB+AAMAAADIdAAMRmxvb3IgYm91bmNlcQB+AAp0ABFBbW91bnQgb2Ygc2hyaW5rc3NxAH4AAwAAAfR0AB9sb2xzcGFzdGllcyAoMC1uOyBCYWQgd2luZG1vZGUpc3EAfgAQAAAAAHQAB1NlYW1vZGVxAH4ADHQADUJhbGwgZGlhbWV0ZXJzcQB+AAMAAAH0dAARQW1vdW50IG9mIGJvdW5jZXNzcQB+AAMAAAACdAAcT2JqZWN0IGNvbG9yIHJlZCBtYXggKDAtMjU1KXNxAH4AAwAAAP90AA9XaW5kc3BlZWQgKDAtbilzcQB+ABBBIAAAdAAOQ2VpbGluZyBib3VuY2VxAH4ACnQAEFNoaWZ0IGVkZ2UgKDAtMSlzcQB+ABAAAAAAdAAUQmFsbCBvcGFjaXR5ICgwLTI1NSlzcQB+ABBAoAAAdAAeT2JqZWN0IGNvbG9yIGdyZWVuIG1heCAoMC0yNTUpc3EAfgADAAAA/3QACUNvbGxpc2lvbnEAfgAMdAAOUGh5c2ljcyB0b2dnbGVxAH4ADHQAHE9iamVjdCBjb2xvciByZWQgbWluICgwLTI1NSlzcQB+AAMAAACWdAAQQmFsbCBzcGVlZCAoMS1uKXNxAH4AEEEgAAB0AAtTZWEgb3BhY2l0eXNxAH4AAwAAAK90AB5PYmplY3QgY29sb3IgZ3JlZW4gbWluICgwLTI1NSlzcQB+AAMAAACWdAAWQmFja2dyb3VuZCByZWQgKDAtMjU1KXNxAH4AAwAAAGR0AAtTdHJva2V3aWR0aHEAfgAWdAAxQmFja2dyb3VuZCBvcGFjaXR5ICgwLTI1NTsgMD1hcnRtb2RlLCAyNTU9b3BhcXVlKXNxAH4AAwAAAAB0AB1PYmplY3QgY29sb3IgYmx1ZSBtaW4gKDAtMjU1KXEAfgA5dAAWU3Ryb2tlIG9wYWNpdHkgKDAtMjU1KXEAfgA5eA==");
		
		ssg.makeGUI("Setup", "Run", "Spawn");
		ssg.setResultListener(this);

		//file = new SoundFile(this, "untitled.wav");
		//file.play();
	} //setup

	boolean ballClips (Ball[] ballen, Ball target, int length) {
		for (int i=0; i<length; i++) {
			if (target.clipsWith(ballen[i])) {
				return true;
			}
		} 
		return false;
	} //boolean Ballclip: checkt of een ball clipt met de lijst ballen

	/*void botse() {
for (int i=0; i<ballen.length; i++) {
Ball ball1 = ballen[i];
for (int j=i+1; j<ballen.length; j++) {
Ball ball2 = ballen[j];
float afstand = ball1.pos.dist(ball2.pos);
if ( afstand < diameter ) {
PVector temp = ball1.speed;
ball1.speed = ball2.speed;
ball2.speed = temp;
ball1.pos.add(ball1.speed);
ball2.pos.add(ball2.speed);
//MES2.add("bang"); // add an int to the osc message
//oscP5.send(MES2, myRemoteLocation); 
//ball1.xSpeed*=-1; ball2.xSpeed*=-1; ball1.ySpeed*=-1; ball2.ySpeed*=-1;
}
}
}
} //botse */ //botse oud

	void botse() {
		for (int i=0; i<ballen.length; i++) {
			Ball ball1 = ballen[i];
			for (int j=i+1; j<ballen.length; j++) {
				Ball ball2 = ballen[j];
				float afstand = ball1.pos.dist(ball2.pos);
				if ( afstand < diameter ) {
					float m1 = mFact * pow(diameter, 3);
					float m2 = mFact * pow(diameter, 3);
					PVector V1 = ball1.speed;
					PVector V2 = ball2.speed;
					PVector dV = (PVector.sub(V1,V2));
					PVector dR = (PVector.sub(ball1.pos, ball2.pos));
					float temp1_1 = 2 * (m1 / (m1 + m2)) * (1 / dR.magSq()); //magSq is lengte in het kwadraat
					float temp1_2 = 2 * (m2 / (m1 + m2)) * (1 / dR.magSq()); //magSq is lengte in het kwadraat
					PVector temp2 = PVector.mult(dR, (PVector.dot(dV, dR)));
					ball1.speed = PVector.sub(V1, PVector.mult(temp2, temp1_1));
					ball2.speed = PVector.add(V2, PVector.mult(temp2, temp1_2));
					//MES2.add("bang"); // add an int to the osc message
					//oscP5.send(MES2, myRemoteLocation);
				}
			}
		}
	} //botse */

	public void draw() {
		if (start) {
			stroke(BACKGROUNDR, BACKGROUNDG, BACKGROUNDB, BACKGROUND_OP);
			fill(BACKGROUNDR, BACKGROUNDG, BACKGROUNDB, BACKGROUND_OP);
			rect(0, 0, breedte, hoogte);  
			//rectangle ipv van background om de opacity te regelen

			if(makeBalls) {
				background(BACKGROUNDR, BACKGROUNDG, BACKGROUNDB);
				ballen = new Ball[nBallen];
				for (int i=0; i<nBallen; i++) {
					Ball newBall = new Ball();                 /* maakt een newBall */
					while (ballClips (ballen, newBall, i) && COLLISION_ENABLED) {    /* Check met ballClips of newBall clipt met de tot dan toe gemaakte balllijst */
						newBall = new Ball();
					}                                        
					ballen[i] = newBall;                     /* voegt newBall toe aan de lijst ballen */
				} //for Deze functie maakt ballen voor de hoeveelheid nBallen
				makeBalls = false;
			} // if Deze if synchroniseerd het maken en het tekenen van ballen

			lolkek = new PVector(random(-lolkekspasties, lolkekspasties), 0);           /* lolkek -1-1 (Alleen de random waardes) */
			wind = swq.getNewWindValue(); // nieuwe windvalue voor wind

			for (int i=0; i<ballen.length; i++) {
				ballen[i].draw();
			} //for: ballen tekenen

			if (COLLISION_ENABLED) {
				botse();
			} //activeert de botsfunctie als collision aan staat

			if (WATER) {
				stroke(250,250,250,0);
				fill(100,100,150,WATER_OP);
				rect(0,hoogte-(hoogte/3),breedte,hoogte);
			} //creeert het water

			/*println(ballen[1].pos.x);
			Pos.add(ballen[1].pos.x);
			println(Pos);
			oscP5.send(Pos, myRemoteLocation); */
		}
	} //draw

	public void mousePressed() {
		ssg.clickButton2();
	} //Reset als muisklik
	
	public void run1(Settings settings){
		FADE_COUNT = settings.get("Amount of bounces");
		FADE_ENABLED = settings.get("Bounce limit toggle"); 
		GRAVITY = settings.get("Gravity (0-1)");
		LUCHTWRIJVING = settings.get("Air Resistance (0-1, default=0.9985)");
		GRAVITY_ENABLED = settings.get("Physics toggle");
		COLLISION_ENABLED = settings.get("Collision");
		//ART = settings.get("Artmode");
		WATER = settings.get("Seamode");
		BALL_OPACITY = settings.get("Ball opacity (0-255)");
		EDGESHIFT = settings.get("Shift edge (0-1)");
		WINDSNELHEID = settings.get("Windspeed (0-n)");
		BALLSHAPE = settings.get("Ball shape (0,1,2; 0=ball)");
		nBallen = settings.get("Ball amount");
		println(settings.get("Ball diameter"));
		int temp = settings.get("Ball diameter");
		DIAMTR = (float)temp;
		snelheid = settings.get("Ball speed (1-n)");
		lolkekspasties = settings.get("lolspasties (0-n; Bad windmode)");
		FLOOR_ENABLED = settings.get("Floor bounce");
		CEILING_ENABLED = settings.get("Ceiling bounce");
		WALLS_ENABLED = settings.get("Wall bounce");
		STROKEWIDTH = settings.get("Strokewidth");
		STROKE_OPACITY = settings.get("Stroke opacity (0-255)");
		BACKGROUNDR = settings.get("Background red (0-255)");
		BACKGROUNDG = settings.get("Background green (0-255)");
		BACKGROUNDB = settings.get("Background blue (0-255)");
		BALLRMIN = settings.get("Object color red min (0-255)");
		BALLRMAX = settings.get("Object color red max (0-255)");
		BALLGMIN = settings.get("Object color green min (0-255)");
		BALLGMAX = settings.get("Object color green max (0-255)");
		BALLBMIN = settings.get("Object color blue min (0-255)");
		BALLBMAX = settings.get("Object color blue max (0-255)");
		BACKGROUND_OP = settings.get("Background opacity (0-255; 0=artmode, 255=opaque)");
		WATER_OP = settings.get("Sea opacity");
		RNDMDIAM = settings.get("Shrink toggle");
		SHRINKAMNT = settings.get("Amount of shrinks");

		diameter = DIAMTR; 
		println(DIAMTR);
		ballradius = diameter/2*EDGESHIFT; 
		gravity = new PVector(0, GRAVITY);
		swq = new SmartWindQueue(130,WINDSNELHEID);
		size(breedte, hoogte);
	}

	public void run2(Settings settings) {
		run1(settings);
		makeBalls = true;
		start = true;
	}
}
