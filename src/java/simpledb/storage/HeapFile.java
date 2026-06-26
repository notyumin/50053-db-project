package simpledb.storage;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Debug;
import simpledb.common.Permissions;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.*;
import java.util.*;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    private File f;
    private TupleDesc td;
    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        this.f = f;
        this.td = td;
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return this.f;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
        return f.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        return this.td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
    	if(pid != null) {
            int pageNo = pid.getPageNumber();
            int offset = pageNo * BufferPool.getPageSize();
            byte[] pageData = new byte[BufferPool.getPageSize()];
            try {
                FileInputStream file = new FileInputStream(f.getAbsoluteFile());
                file.skip(offset);
                file.read(pageData);
                file.close();
                return new HeapPage((HeapPageId)pid, pageData);

            } catch(Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    	return null;
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
    	return (int) Math.ceil(f.length() / BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public List<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
        return new DbFileIterator() {
            private int currentPage = -1;
            private Iterator<Tuple> tupleIterator = null;

            public void open() throws DbException, TransactionAbortedException {
                currentPage = 0;
                tupleIterator = getPageIterator(currentPage);
            }

            private Iterator<Tuple> getPageIterator(int pageNo)
                    throws DbException, TransactionAbortedException {
                HeapPageId pid = new HeapPageId(getId(), pageNo);
                HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid, pid, Permissions.READ_ONLY);
                return page.iterator();
            }

            public boolean hasNext() throws DbException, TransactionAbortedException {
                if (tupleIterator != null) {

                    if (tupleIterator.hasNext()) return true;
                    while (currentPage < numPages() - 1) {
                        currentPage++;
                        tupleIterator = getPageIterator(currentPage);
                        if (tupleIterator.hasNext()) { return true; }
                    }
                }
                

                return false;
            }

            public Tuple next() throws DbException, TransactionAbortedException {
                if (!hasNext()) { throw new NoSuchElementException(); }
                return tupleIterator.next();
            }

            public void rewind() throws DbException, TransactionAbortedException {
                close();
                open();
            }

            public void close() {
                currentPage = -1;
                tupleIterator = null;
            }
        };
    }
}
