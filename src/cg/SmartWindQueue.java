package cg;
import java.util.ArrayDeque;

public class SmartWindQueue {
  ArrayDeque<Float> randQueue = new ArrayDeque<Float>();
  float speedWindSnelheid;
  
  SmartWindQueue(int length, float XTC) {
    speedWindSnelheid = XTC;
    for (int i = 0; i < length; i++) {
      randQueue.add(random(-speedWindSnelheid, speedWindSnelheid));
    }
  }
  
  float getNewWindValue() {
    float newVal = random(-speedWindSnelheid, speedWindSnelheid);
    if (newVal >= speedWindSnelheid * 0.8 || newVal <= speedWindSnelheid * -0.8) {
      newVal *= 2;
    }
    randQueue.offer(newVal);
    randQueue.poll();
    Float[] floats = new Float[randQueue.size()];
    randQueue.toArray(floats);
    float average = 0;
    for (Float f : floats) {
      average += f;
    }
    average /= randQueue.size();
    return average;
  }
  
  private float random(float min, float max) {
	float rand = (float) Math.random();
	return (rand * (max - min) + min);
  }
}
