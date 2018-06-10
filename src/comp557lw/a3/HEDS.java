package comp557lw.a3;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Half edge data structure.
 * Maintains a list of faces (i.e., one half edge of each) to allow for easy display of geometry.
 * 
 * @author : Kevin Sun
 */
public class HEDS {

    /**
     * List of faces 
     */
    List<Face> faces = new ArrayList<Face>();
        
    /**
     * Constructs an empty mesh (used when building a mesh with subdivision)
     */
    public HEDS() {
        // do nothing
    }
        
    /**
     * Builds a half edge data structure from the polygon soup   
     * @param soup
     */
    public HEDS( PolygonSoup soup ) {
    	ArrayList<ArrayList<HalfEdge>> heAtTail = new ArrayList<ArrayList<HalfEdge>>(soup.vertexList.size());
    	for (int i = 0; i < soup.vertexList.size(); i++) {
    		heAtTail.add(new ArrayList<HalfEdge>());
    	}
        for (int[] incs : soup.faceList) {
        	ArrayList<HalfEdge> hes = new ArrayList<HalfEdge>();
        	for (int i = 0; i < incs.length; i++) {
        		int tail = i;
        		int head = i+1;
        		if (head >= incs.length) head = 0;
        		HalfEdge he = new HalfEdge();
        		he.head = soup.vertexList.get(incs[head]);
        		//store in tail order for twin searching
        		heAtTail.get(incs[tail]).add(he);
        		//search for potential twins
        		for (HalfEdge search : heAtTail.get(incs[head])) {
        			if (search.head.equals(soup.vertexList.get(incs[tail]))) {
        				he.twin = search;
        				search.twin = he;
        			}
        		}
        		hes.add(he);
        	}
        	//assigning next
        	for (int i = 0; i < hes.size(); i++) {
        		int next = i + 1;
        		if (next >= hes.size()) next = 0;
        		hes.get(i).next = hes.get(next);
        	}
        	// creating faces
        	this.faces.add(new Face(hes.get(0)));
        }
        
        
    } 
    
    /**
     * Draws the half edge data structure by drawing each of its faces.
     * Per vertex normals are used to draw the smooth surface when available,
     * otherwise a face normal is computed. 
     * @param drawable
     */
    public void display() {
        // note that we do not assume triangular or quad faces, so this method is slow! :(     
        Point3d p;
        Vector3d n;        
        for ( Face face : faces ) {
            HalfEdge he = face.he;
            if ( he.head.n == null ) { // don't have per vertex normals? use the face
                glBegin( GL_POLYGON );
                n = he.leftFace.n;
                glNormal3d( n.x, n.y, n.z );
                HalfEdge e = he;
                do {
                    p = e.head.p;
                    glVertex3d( p.x, p.y, p.z );
                    e = e.next;
                } while ( e != he );
                glEnd();
            } else {
                glBegin( GL_POLYGON );                
                HalfEdge e = he;
                do {
                    p = e.head.p;
                    n = e.head.n;
                    glNormal3d( n.x, n.y, n.z );
                    glVertex3d( p.x, p.y, p.z );
                    e = e.next;
                } while ( e != he );
                glEnd();
            }
        }
    }
    
    /** 
     * Draws all child vertices to help with debugging and evaluation.
     * (this will draw each points multiple times)
     * @param drawable
     */
    public void drawChildVertices() {
    	glDisable( GL_LIGHTING );
        glPointSize(8);
        glBegin( GL_POINTS );
        for ( Face face : faces ) {
            if ( face.child != null ) {
                Point3d p = face.child.p;
                glColor3f(0,0,1);
                glVertex3d( p.x, p.y, p.z );
            }
            HalfEdge loop = face.he;
            do {
                if ( loop.head.child != null ) {
                    Point3d p = loop.head.child.p;
                    glColor3f(1,0,0);
                    glVertex3d( p.x, p.y, p.z );
                }
                if ( loop.child1 != null && loop.child1.head != null ) {
                    Point3d p = loop.child1.head.p;
                    glColor3f(0,1,0);
                    glVertex3d( p.x, p.y, p.z );
                }
                loop = loop.next;
            } while ( loop != face.he );
        }
        glEnd();
        glEnable( GL_LIGHTING );
    }
    
    public void addFace(Face f) {
    	this.faces.add(f);
    }
}
