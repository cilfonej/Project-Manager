package managment.conflict;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import com.google.api.services.drive.model.Revision;

import managment.Root;
import managment.storage.DriveInterface;
import managment.storage.SerializationContext.MutedSerializationContext;

public class DiffFinder {
	public static void main(String[] args) {
//		ArrayList<Revision> revisions = DriveInterface.getInstance().getRevisions();
//		for(Object object : new DiffFinder(
//				DriveInterface.getInstance().downloadRevision(revisions.get(revisions.size() - 25)), 
//				DriveInterface.getInstance().downloadRevision(revisions.get(revisions.size() - 24))).findDiffereces())
//			System.out.println(object);
	}
	
	private MutedSerializationContext prevContext;
	private MutedSerializationContext nextContext;
	
	public DiffFinder(File prevFile, File nextFile) {
		prevContext = new MutedSerializationContext(prevFile);
		nextContext = new MutedSerializationContext(nextFile);
	}
	
	public ArrayList<Object> findDiffereces() {
		HashSet<Integer> prevUnclaimed = new HashSet<>();
		for(int i = 0; i < prevContext.getCount(); i ++) prevUnclaimed.add(i);

		HashSet<Integer> nextUnclaimed = new HashSet<>();
		for(int i = 0; i < nextContext.getCount(); i ++) nextUnclaimed.add(i);
		
		byteMatchSearch:
		for(Iterator<Integer> nextIter = nextUnclaimed.iterator(); nextIter.hasNext();) {
			int nextIndex = nextIter.next();
			for(Iterator<Integer> prevIter = prevUnclaimed.iterator(); prevIter.hasNext();) {
				int prevIndex = prevIter.next();
				
				if(Arrays.equals(nextContext.getRaw(nextIndex), prevContext.getRaw(prevIndex))) {
					nextIter.remove();
					prevIter.remove();
					continue byteMatchSearch;
				}
			}
		}
		
		ArrayList<Object> changes = new ArrayList<>();
		
		if(!prevUnclaimed.isEmpty()) {
//			prevContext.load(Root.class, 0);

			for(Iterator<Integer> prevIter = prevUnclaimed.iterator(); prevIter.hasNext();) 
				changes.add(prevContext.loadRaw(prevIter.next()));
		}
		
		if(!nextUnclaimed.isEmpty()) {
//			nextContext.load(Root.class, 0);
			
			for(Iterator<Integer> nextIter = nextUnclaimed.iterator(); nextIter.hasNext();) 
				changes.add(nextContext.loadRaw(nextIter.next()));
		}
			
		return changes;
	}
}
