package org.andengine.extension.rubeloader;

import java.io.IOException;

import net.minidev.json.parser.ParseException;

import org.andengine.entity.IEntity;
import org.andengine.extension.rubeloader.def.RubeDef;
import org.andengine.extension.rubeloader.factory.EntityFactory;
import org.andengine.extension.rubeloader.factory.IEntityFactory;
import org.andengine.extension.rubeloader.factory.IPhysicsWorldFactory;
import org.andengine.extension.rubeloader.factory.IPhysicsWorldProvider;
import org.andengine.extension.rubeloader.parser.RubeParser;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.StreamUtils;
import org.andengine.util.adt.io.in.ResourceInputStreamOpener;
import org.andengine.util.debug.Debug;

import android.content.res.Resources;

/**
 * Simple example of a customr R.U.B.E. loader
 * @author Michal Stawinski (nazgee)
 */
public class RubeLoader {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected final RubeParser mRubeParser;
	protected final IEntityFactory mEntityFactory;

	// ===========================================================
	// Constructors
	// ===========================================================
	public RubeLoader() {
		this(new EntityFactory());
	}

	public RubeLoader(final IEntityFactory pEntityFactory) {
		this.mRubeParser = new RubeParser(pEntityFactory);
		this.mEntityFactory = pEntityFactory;
	}

	public RubeLoader(final IPhysicsWorldFactory pPhysicsWorldFactory, final IEntityFactory pEntityFactory) {
		this.mRubeParser = new RubeParser(pPhysicsWorldFactory, pEntityFactory);
		this.mEntityFactory = pEntityFactory;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	public RubeDef loadMoreToExistingWorld(final Resources pResources, final IEntity pSceneEntity, final ITextureProvider pTextureProvider, final VertexBufferObjectManager pVBOM, int resId, final IPhysicsWorldProvider pPhysicsWorldProvider) {

		return loadMoreToExistingWorld(readResource(resId, pResources), pSceneEntity, pTextureProvider, pVBOM, pPhysicsWorldProvider);
	}
	
	public RubeDef loadMoreToExistingWorld(final Resources pResources, final IEntity pSceneEntity, final ITextureProvider pTextureProvider, final VertexBufferObjectManager pVBOM, final String pPathString, final IPhysicsWorldProvider pPhysicsWorldProvider) {

		return loadMoreToExistingWorld(readAsset(pPathString, pResources), pSceneEntity, pTextureProvider, pVBOM, pPhysicsWorldProvider);
	}
	
	public RubeDef loadMoreToExistingWorld(final String jsonString, final IEntity pSceneEntity, final ITextureProvider pTextureProvider, final VertexBufferObjectManager pVBOM, final IPhysicsWorldProvider pPhysicsWorldProvider) {
		long startTime = System.currentTimeMillis();

		this.mEntityFactory.configure(pSceneEntity, pTextureProvider, pVBOM);

		RubeDef rube;
		try {
			rube = mRubeParser.continueParse(pPhysicsWorldProvider, jsonString);
		} catch (ParseException e) {
			throw new RuntimeException("RUBE json parsing failed! ", e);
		}

		long elapseTime = System.currentTimeMillis() - startTime;
		Debug.w("RubeLoaderExtension LOAD_TIME=" + elapseTime/1000.f);

		return rube;
	}

	public RubeDef load(final Resources pResources, final IEntity pSceneEntity, final ITextureProvider pTextureProvider, final VertexBufferObjectManager pVBOM, int resId) {
		long startTime = System.currentTimeMillis();

		this.mEntityFactory.configure(pSceneEntity, pTextureProvider, pVBOM);

		RubeDef rube;
		try {
			rube = mRubeParser.parse(readResource(resId, pResources));
		} catch (ParseException e) {
			throw new RuntimeException("RUBE json parsing failed! ", e);
		}

		long elapseTime = System.currentTimeMillis() - startTime;
		Debug.w("RubeLoaderExtension LOAD_TIME=" + elapseTime/1000.f);

		return rube;
	}

	public static String readResource(int resId, Resources pResources) {
		try {
			return StreamUtils.readFully(new ResourceInputStreamOpener(pResources, resId).open());
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
	}

	public static String readAsset(final String assetPath, final Resources pResources) {
		try {
			return StreamUtils.readFully(pResources.getAssets().open(assetPath));
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
