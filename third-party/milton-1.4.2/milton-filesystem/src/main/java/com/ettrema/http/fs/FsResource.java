package com.ettrema.http.fs;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import java.io.File;
import java.util.Date;

/**
 *
 */
public abstract class FsResource implements Resource, MoveableResource, CopyableResource, LockableResource {
    File file;
    final FileSystemResourceFactory factory;

    protected abstract void doCopy(File dest);
    
    public FsResource(FileSystemResourceFactory factory, File file) {
        this.file = file;
        this.factory = factory;
    }
    
    public File getFile() {
        return file;
    }
    
    public String getUniqueId() {
        String s = file.lastModified() + "_" + file.length();
        return s.hashCode() + "";
    }

    public String getName() {
        return file.getName();
    }

    public Object authenticate(String user, String password) {
        return factory.getSecurityManager().authenticate(user, password);
    }

    public boolean authorise(Request request, Method method, Auth auth) {
        return factory.getSecurityManager().authorise(request, method, auth, this);
    }

    public String getRealm() {
        return factory.getRealm();
    }

    public Date getModifiedDate() {
        return new Date(file.lastModified());
    }

    public Date getCreateDate() {
        return null;
    }
    
    public int compareTo(Resource o) {
        return this.getName().compareTo(o.getName());
    }

    public void moveTo(CollectionResource newParent, String newName) {
        if( newParent instanceof FsDirectoryResource ) {
            FsDirectoryResource newFsParent = (FsDirectoryResource) newParent;
            File dest = new File(newFsParent.getFile(), newName);
            boolean ok = this.file.renameTo(dest);
            if( !ok ) throw new RuntimeException("Failed to move to: " + dest.getAbsolutePath());
            this.file = dest;
        } else {
            throw new RuntimeException("Destination is an unknown type. Must be a FsDirectoryResource, is a: " + newParent.getClass());
        }
    }
    
    public void copyTo(CollectionResource newParent, String newName) {
        if( newParent instanceof FsDirectoryResource ) {
            FsDirectoryResource newFsParent = (FsDirectoryResource) newParent;
            File dest = new File(newFsParent.getFile(), newName);
            doCopy(dest);
        } else {
            throw new RuntimeException("Destination is an unknown type. Must be a FsDirectoryResource, is a: " + newParent.getClass());
        }        
    }

    public void delete() {
        boolean ok = file.delete();
        if( !ok ) throw new RuntimeException("Failed to delete");
    }

    public LockResult lock(LockTimeout timeout, LockInfo lockInfo) {
        return factory.getLockManager().lock(timeout, lockInfo, this);
    }

    public LockResult refreshLock(String token) {
        return factory.getLockManager().refresh(token, this);
    }

    public void unlock(String tokenId) {
        factory.getLockManager().unlock(tokenId, this);
    }

    public LockToken getCurrentLock() {
        return factory.getLockManager().getCurrentToken( this );
    }



}
