package comp557lw.a3;

import java.util.ArrayList;

/**
 * Class implementing the Catmull-Clark subdivision scheme
 * 
 * @author : Kevin Sun
 */
public class CatmullClark {

    /**
     * Subdivides the provided half edge data structure
     * @param heds
     * @return the subdivided mesh
     */
    public static HEDS subdivide( HEDS heds ) {
        HEDS heds2 = new HEDS();
        
        
        //get boundary
        ArrayList<HalfEdge> boundary = new ArrayList<HalfEdge>();
        
        for (Face f : heds.faces) {
        	HalfEdge loop = f.he;
        	do {
        		if (loop.twin == null) {
        			boundary.add(loop);
        		}
        		loop = loop.next;
        	} while (loop != f.he);
        }
        //even vertices 
        for (Face f : heds.faces) {
        	HalfEdge loop = f.he;
            do {
            	boolean isBoundary = loop.twin == null;
        		HalfEdge trans;
        		int k = 0;
        		if (!isBoundary) {
        			trans = loop;
            		do {
            			trans = trans.next;
            			trans = trans.twin;
            			if (trans == null) {
            				isBoundary = true;
            				break;
            			}
            			else {
            				k++;
            			}
            		} while (trans != loop);
        		}
        		
        		if (isBoundary) {
        			Vertex prev = null, next = null;
        			for (HalfEdge he : boundary) {
        				if (he.prev().head.equals(loop.head)) {
        					next = he.head;
        				}
        				if (he.head.equals(loop.head)) {
        					prev = he.prev().head;
        				}
        			}
                    Vertex newChild = new Vertex();
                    newChild.p.x = ((double)1/8)*prev.p.x + ((double)6/8)*loop.head.p.x + ((double)1/8)*next.p.x;
                    newChild.p.y = ((double)1/8)*prev.p.y + ((double)6/8)*loop.head.p.y + ((double)1/8)*next.p.y;
                    newChild.p.z = ((double)1/8)*prev.p.z + ((double)6/8)*loop.head.p.z + ((double)1/8)*next.p.z;
                    loop.head.child = newChild;
        		} else {
        			double beta = ((double)1/k*((double)5/8-Math.pow((double)3/8 + (double)1/4*Math.cos(2*Math.PI/k), 2)));
        			Vertex newChild = new Vertex();
        			double xSum = 0, ySum = 0, zSum = 0;
        			trans = loop;
        			do {
        				xSum += beta * trans.twin.head.p.x;
        				ySum += beta * trans.twin.head.p.y;
        				zSum += beta * trans.twin.head.p.z;
        				trans = trans.next;
        				trans = trans.twin;
        			} while (trans != loop);
        			newChild.p.x = xSum + (1-k*beta)*loop.head.p.x;
        			newChild.p.y = ySum + (1-k*beta)*loop.head.p.y;
        			newChild.p.z = zSum + (1-k*beta)*loop.head.p.z;
        			loop.head.child = newChild;
        		}
                loop = loop.next;
            } while ( loop != f.he );
        }
        
        for (Face f : heds.faces) {
        	//odd vertices
        	f.getChild();
        	HalfEdge loop = f.he;
            do {
            	if (loop.child1 == null && loop.child2 == null) {
                	Vertex mid = new Vertex();
                	if (loop.twin == null) {
                		//boundary
                    	mid.p.x = 0.5*loop.head.p.x + 0.5*loop.prev().head.p.x;
                    	mid.p.y = 0.5*loop.head.p.y + 0.5*loop.prev().head.p.y;
                    	mid.p.z = 0.5*loop.head.p.z + 0.5*loop.prev().head.p.z;
                	}
                	else {
                    	mid.p.x = 0.25*loop.leftFace.getChild().p.x + 0.25*loop.twin.leftFace.getChild().p.x + 0.25*loop.head.p.x + 0.25*loop.prev().head.p.x;
                    	mid.p.y = 0.25*loop.leftFace.getChild().p.y + 0.25*loop.twin.leftFace.getChild().p.y + 0.25*loop.head.p.y + 0.25*loop.prev().head.p.y;
                    	mid.p.z = 0.25*loop.leftFace.getChild().p.z + 0.25*loop.twin.leftFace.getChild().p.z + 0.25*loop.head.p.z + 0.25*loop.prev().head.p.z;
                	}
                	HalfEdge one = new HalfEdge();
                	HalfEdge two = new HalfEdge();
                	one.head = mid;
                	two.head = loop.head.child;
                	
                	one.parent = loop;
                	two.parent = loop;
                	loop.child1 = one;
                	loop.child2 = two;
                	
                	if (loop.twin != null) {	
                    	HalfEdge twinOne = new HalfEdge();
                    	HalfEdge twinTwo = new HalfEdge();
                    	twinOne.head = mid;
                    	twinTwo.head = loop.twin.head.child;
                    	one.twin = twinTwo;
                    	twinTwo.twin = one;
                    	two.twin = twinOne;
                    	twinOne.twin = two;
                    	
                    	twinOne.parent = loop.twin;
                    	twinTwo.parent = loop.twin;
                    	loop.twin.child1 = twinOne;
                    	loop.twin.child2 = twinTwo;
                	}
            	}
                loop = loop.next;
            } while ( loop != f.he );
            
            //connectivity
            loop = f.he;
            HalfEdge lastChildToCenter = null;
            do {
            	HalfEdge newHe = new HalfEdge();
            	HalfEdge newHeNext = new HalfEdge();
            	newHe.head = f.getChild();
            	newHeNext.head = loop.prev().child1.head;
                loop.child1.next = newHe;
                newHe.next = newHeNext;
                newHeNext.next = loop.prev().child2;
                loop.prev().child2.next = loop.child1;
                if (lastChildToCenter != null) {
                	lastChildToCenter.twin = newHeNext;
                	newHeNext.twin = lastChildToCenter;
                }
                lastChildToCenter = newHe;
                
                loop = loop.next;
            } while ( loop != f.he );
            f.he.child1.next.next.twin = lastChildToCenter;
            lastChildToCenter.twin = f.he.child1.next.next;
            
            loop = f.he;
            do {
                heds2.addFace(new Face(loop.child1));;
                loop = loop.next;
            } while ( loop != f.he );
        }
        
        return heds2;        
    }
    
}
