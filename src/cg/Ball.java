package cg;
import processing.core.PApplet;
import processing.core.PVector;
import static cg.Classesgaaf.in;

public class Ball {
  PVector pos, speed;
  int stuitercount;
  int kleur, kleur2;
  
  Ball() {
    float xSpeed=in.random(-in.snelheid,in.snelheid);
    while (xSpeed < 1 && xSpeed > -1 && in.snelheid >= 1) {
       xSpeed=in.random(-in.snelheid,in.snelheid);
    }
    float ySpeed=in.random(-in.snelheid,in.snelheid);
     while (ySpeed < 1 && ySpeed > -1 && in.snelheid >= 1) {
       ySpeed=in.random(-in.snelheid,in.snelheid);
    }
    
    pos = new PVector(in.random(in.ballradius,in.breedte-in.ballradius),
    		in.random(in.ballradius,in.hoogte-in.ballradius));
    speed = new PVector(xSpeed, ySpeed);
    
    kleur = in.color(in.random(in.BALLRMIN, in.BALLRMAX), in.random(in.BALLGMIN, in.BALLGMAX),
    		in.random(in.BALLBMIN, in.BALLBMAX), in.BALL_OPACITY);
    //kleur2 = in.color(in.random(in.BALLRMIN, in.BALLRMAX), in.random(in.BALLGMIN, in.BALLGMAX), in.random(in.BALLBMIN, in.BALLBMAX), STROKE_OPACITY);
    kleur2 = in.color(0, 0, 0, in.STROKE_OPACITY);
    stuitercount = in.FADE_COUNT;
  } //Ball()
  
 public void draw() {
   
  in.stroke(kleur2);
  in.fill(kleur);
  in.strokeWeight(in.STROKEWIDTH);
  
  //BALLSHAPE = int(in.random (4)); /* voor cool effect!! */
  
  in.ballradius = in.diameter/2*in.EDGESHIFT;  
  if (in.diameter <= 0) {
	  in.diameter = 0;
  };
  
  if (in.BALLSHAPE == 0) {
	  in.ellipse(pos.x, pos.y, in.diameter, in.diameter);
  } else if (in.BALLSHAPE == 1) {
	  in.ellipse(pos.x, pos.y, in.diameter, in.diameter);
	  in.ellipse(pos.x, pos.y-in.diameter, in.diameter/2, in.diameter/2);
	  in.ellipse(pos.x, pos.y-in.diameter*2, in.diameter/5, in.diameter/5);
  } else if (in.BALLSHAPE == 2) {
	  in.triangle(pos.x - 0.5f * in.diameter, pos.y - 0.5f * in.diameter, pos.x + 0.5f * in.diameter, pos.y - 0.5f * in.diameter, pos.x, pos.y + 0.5f * in.diameter);
  } else if (in.BALLSHAPE == 3) {
	  in.ellipse(pos.x, pos.y-in.diameter/1.5f, in.diameter/2, in.diameter/2);
	  in.ellipse(pos.x, pos.y+in.diameter/1.5f, in.diameter/2, in.diameter/2);
	  in.ellipse(pos.x+in.diameter/1.5f, pos.y, in.diameter/2, in.diameter/2);
	  in.ellipse(pos.x-in.diameter/1.5f, pos.y, in.diameter/2, in.diameter/2);
	  in.ellipse(pos.x, pos.y, in.diameter, in.diameter);
  } else if (in.BALLSHAPE == 4) {
	  in.ellipse(pos.x, pos.y, in.diameter, in.diameter);
	  in.ellipse(pos.x-in.diameter/2, pos.y-in.diameter/2, in.diameter/1.5f, in.diameter/1.5f);
	  in.ellipse(pos.x+in.diameter/2, pos.y-in.diameter/2, in.diameter/1.5f, in.diameter/1.5f);
  } //else if (BALLSHAPE == 5) {
  
  
  if (in.GRAVITY_ENABLED) {
  speed.add(in.gravity);
  speed.add(in.lolkek);
  speed.x += (in.wind * (1 - (pos.y / in.hoogte)));
  speed.mult(in.LUCHTWRIJVING); /* luchtwrijving vermenigvuldiging */
  }
  pos.add(speed); /* snelheid implementatie */
  
  in.diameter = (in.diameter + (in.DIAMTR / 10)) % (in.DIAMTR * 3) ;

  //if (!fallen) { 
  if (stuitercount > 0 || !in.FADE_ENABLED) {
  
    if (in.WALLS_ENABLED) {
      if ((pos.x <= in.ballradius && speed.x <= 0) || (pos.x >= in.breedte - in.ballradius && speed.x >= 0)) {
        speed.x = -speed.x;
        pos.add(speed);
        kleur = in.color(in.random(in.BALLRMIN, in.BALLRMAX), in.random(in.BALLGMIN, in.BALLGMAX), in.random(in.BALLBMIN, in.BALLBMAX), in.BALL_OPACITY);
        stuitercount--;
        in.oscP5.send(in.WallMes, in.myRemoteLocation); 
        if (in.RNDMDIAM) {
        //in.diameter = (int(in.random(0.5 * DIAMTR, 1.5 * DIAMTR)));
        in.diameter = (in.diameter - (in.DIAMTR / in.SHRINKAMNT)) ;
        }
      } //if: stuiter van de zijkanten af
    } else {
      if ((pos.x <= in.ballradius && speed.x <= 0) || (pos.x >= in.breedte - in.ballradius && speed.x >= 0)) {
        pos.x = pos.x % in.breedte;
        if (pos.x < 0 ) {
          pos.x += in.breedte;
        }
        kleur = in.color(in.random(in.BALLRMIN, in.BALLRMAX), in.random(in.BALLGMIN, in.BALLGMAX), in.random(in.BALLBMIN, in.BALLBMAX), in.BALL_OPACITY);
        stuitercount--;
        in.oscP5.send(in.WallMes, in.myRemoteLocation); 
      }
    }//if: alle muurstuiterfuncties

   if (in.CEILING_ENABLED) { 
      if (pos.y <= in.ballradius && speed.y <= 0)
        {
          speed.y = -speed.y;
          pos.add(speed);
          kleur = in.color(in.random(in.BALLRMIN, in.BALLRMAX), in.random(in.BALLGMIN, in.BALLGMAX), in.random(in.BALLBMIN, in.BALLBMAX), in.BALL_OPACITY);
          stuitercount--;
          in.oscP5.send(in.CeilingMes, in.myRemoteLocation); 
          if (in.RNDMDIAM) {
        //in.diameter = (int(in.random(0.5 * DIAMTR, 1.5 * DIAMTR)));
          in.diameter = (in.diameter - (in.DIAMTR / in.SHRINKAMNT)) ;            
          }
        } //if: stuiter van de bovenkant af
   } //if: alle plafondstuiter functies
    
   if (in.FLOOR_ENABLED) { 
     if (pos.y >= in.hoogte - in.ballradius && speed.y >= 0)
      {
        speed.y = -speed.y;
        pos.add(speed);
        kleur = in.color(in.random(in.BALLRMIN, in.BALLRMAX), in.random(in.BALLGMIN, in.BALLGMAX), in.random(in.BALLBMIN, in.BALLBMAX), in.BALL_OPACITY);
        stuitercount--;
        in.oscP5.send(in.FloorMes, in.myRemoteLocation); 
        if (in.RNDMDIAM) {
        //in.diameter = (int(in.random(0.5 * DIAMTR, 1.5 * DIAMTR)));
        in.diameter = (in.diameter - (in.DIAMTR / in.SHRINKAMNT)) ;
        }
      } //if: stuiter van de onderkant af */
     
     if (pos.y >= in.hoogte - in.ballradius && speed.y >= -5) 
     {
        speed.y += (pos.y - in.hoogte) / 10; 
     } //if: Als de bal ondergronds komt wordt hij omhooggestuurd met een snelheid afhankelijk van de diepte
   } //if: alle vloerstuiter functies
   
   // waterstuiter:
    if (pos.y >= in.hoogte - (in.hoogte / 3) && speed.y >= -15 && in.WATER) {
        speed.y -= PApplet.exp((pos.y - (in.hoogte-(in.hoogte/3)))/500);
      } //if: Als de bal ondergronds komt wordt hij omhooggestuurd met een snelheid afhankelijk van de diepte
      
  } //if: zet stuiteren uit als de condities bereikt worden
  //} //if: valcheckfunctie (disabled)
 } //draw
 
 public boolean clipsWith(Ball other) {
   return pos.dist(other.pos) < in.diameter;
 } // boolean clipsWith: checkt of de aangeroepen ball clipt met de opgegeven ball

} //Ball