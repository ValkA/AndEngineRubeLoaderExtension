package org.andengine.extension.rubeloader.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import net.minidev.json.parser.ParseException;

import org.andengine.entity.IEntity;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.rubeloader.ITextureProvider;
import org.andengine.extension.rubeloader.def.ImageDef;
import org.andengine.extension.rubeloader.def.RubeDef;
import org.andengine.extension.rubeloader.factory.BodyFactory;
import org.andengine.extension.rubeloader.factory.EntityFactory;
import org.andengine.extension.rubeloader.factory.FixtureFactory;
import org.andengine.extension.rubeloader.factory.IBodyFactory;
import org.andengine.extension.rubeloader.factory.IEntityFactory;
import org.andengine.extension.rubeloader.factory.IFixtureFactory;
import org.andengine.extension.rubeloader.factory.IJointsFactory;
import org.andengine.extension.rubeloader.factory.IPhysicsWorldFactory;
import org.andengine.extension.rubeloader.factory.IPhysicsWorldProvider;
import org.andengine.extension.rubeloader.factory.JointsFactory;
import org.andengine.extension.rubeloader.factory.PhysicsWorldFactory;
import org.andengine.extension.rubeloader.factory.PhysicsWorldProvider;
import org.andengine.extension.rubeloader.json.AutocastMap;
import org.andengine.extension.rubeloader.parser.AdapterListToParser.IInflatingListener;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;


public class RubeParser extends ParserDef<RubeDef> {

	// ===========================================================
	// Constants
	// ===========================================================

	ParserBodyDef mParserBodyDef = new ParserBodyDef();
	ParserFixtureDef mParserFixtureDef = new ParserFixtureDef();
	ParserJointDef mParserJointDef = new ParserJointDef();
	ParserImageDef mParserImageDef = new ParserImageDef();

	AdapterListToParserDef<BodyDef> mParserBodies = new AdapterListToParserDef<BodyDef>("body", mParserBodyDef);
	AdapterListToParserDef<FixtureDef> mParserFixtures = new AdapterListToParserDef<FixtureDef>("fixture", mParserFixtureDef);
	AdapterListToParserDef<JointDef> mParserJoints = new AdapterListToParserDef<JointDef>("joint", mParserJointDef);
	AdapterListToParserDef<ImageDef> mParserImages = new AdapterListToParserDef<ImageDef>("image", mParserImageDef);

	// ===========================================================
	// Fields
	// ===========================================================

	private IPhysicsWorldFactory mPhysicsWorldFactory;
	private final IJointsFactory mJointsFactory;
	private final IFixtureFactory mFixtureFactory;
	private final IBodyFactory mBodyFactory;
	private IEntityFactory mEntityFactory;

	// ===========================================================
	// Constructors
	// ===========================================================

	public RubeParser(final IEntityFactory pEntityFactory) {
		this(new PhysicsWorldFactory(), new BodyFactory(), new FixtureFactory(), new JointsFactory(), pEntityFactory);
	}

	public RubeParser(IPhysicsWorldFactory pPhysicsWorldFactory, final IEntityFactory pEntityFactory) {
		this(pPhysicsWorldFactory, new BodyFactory(), new FixtureFactory(), new JointsFactory(), pEntityFactory);
	}

	public RubeParser(IEntity pSceneEntity, ITextureProvider pTextureProvider, VertexBufferObjectManager pVBOM) {
		this(new PhysicsWorldFactory(), new BodyFactory(), new FixtureFactory(), new JointsFactory(), new EntityFactory(pSceneEntity, pTextureProvider, pVBOM));
	}


	public RubeParser(IPhysicsWorldFactory pPhysicsWorldFactory, IBodyFactory pBodyFactory, IFixtureFactory pFixtureFactory, IJointsFactory pJointsFactory, IEntityFactory pEntityFactory) {
		this.mPhysicsWorldFactory = pPhysicsWorldFactory;
		this.mBodyFactory = pBodyFactory;
		this.mFixtureFactory = pFixtureFactory;
		this.mJointsFactory = pJointsFactory;
		this.setEntityFactory(pEntityFactory);
	}

	@Override
	protected RubeDef doParse(AutocastMap pMap) {
		return doParse(new RubeDef(), pMap);
	}

	public RubeDef continueParse(IPhysicsWorldProvider pPhysicsWorldProvider, String pStringToParse) throws ParseException {
		AutocastMap map = loadMapFromString(pStringToParse);
		return doParse(new RubeDef(pPhysicsWorldProvider), map);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public IEntityFactory getEntityFactory() {
		return mEntityFactory;
	}

	public void setEntityFactory(IEntityFactory mEntityFactory) {
		this.mEntityFactory = mEntityFactory;
	}

	public IPhysicsWorldFactory getPhysicsWorldFactory() {
		return mPhysicsWorldFactory;
	}

	public void setPhysicsWorldFactory(IPhysicsWorldFactory pPhysicsWorldFactory) {
		mPhysicsWorldFactory = pPhysicsWorldFactory;
	}
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	protected RubeDef doParse(RubeDef rubeDef, AutocastMap pMap) {

		mParserBodies.setParsingListener(new BasicListener<BodyDef>());
		mParserFixtures.setParsingListener(new BasicListener<FixtureDef>());
		mParserJoints.setParsingListener(new BasicListener<JointDef>());
		mParserImages.setParsingListener(new BasicListener<ImageDef>());

		if (rubeDef.worldProvider == null) {
			rubeDef.worldProvider = new PhysicsWorldProvider(pMap, this.mPhysicsWorldFactory);
		}

		mParserBodies.parse(pMap);
		mParserJoints.parse(pMap);
		mParserImages.parse(pMap);

		List<BodyDef> bodydefs = mParserBodies.getInflatedResult();
		List<AutocastMap> bodymaps = mParserBodies.getInflatedMapsList();
		List<ArrayList<AutocastMap>> bodycustoms = mParserBodies.getInflatedCustomPropertiesList();
		List<JointDef> jointdefs = mParserJoints.getInflatedResult();
		List<AutocastMap> jointmaps = mParserJoints.getInflatedMapsList();
		List<ArrayList<AutocastMap>> jointcustoms = mParserJoints.getInflatedCustomPropertiesList();
		List<ImageDef> imagedefs = mParserImages.getInflatedResult();
		List<AutocastMap> imagemaps = mParserImages.getInflatedMapsList();
		List<ArrayList<AutocastMap>> imagecustoms = mParserImages.getInflatedCustomPropertiesList();

		int bodycount = (bodydefs != null) ? bodydefs.size() : 0;
		int jointcount = (jointdefs != null) ? jointdefs.size() : 0;
		int imagecount = (imagedefs != null) ? imagedefs.size() : 0;

		rubeDef.primitives.assureCapacities(bodycount, jointcount, imagecount);

		Vector<Body> bodies = rubeDef.primitives.bodies;
		Vector<Joint> joints = rubeDef.primitives.joints;
		Vector<IEntity> images = rubeDef.primitives.images;

		PhysicsWorld world = rubeDef.worldProvider.getWorld();

		/* bodies */
		for (int i = 0; i < bodycount; i++) {
			Body b = mBodyFactory.produce(world, bodydefs.get(i), bodymaps.get(i));
			rubeDef.registerBody(b, i, bodymaps.get(i).getString("name", ""));

			installCustomProps(rubeDef, b, bodycustoms.get(i));

			/* fixtures */
			mParserFixtures.parse(bodymaps.get(i));
			List<FixtureDef> fixturedefs = mParserFixtures.getInflatedResult();
			List<AutocastMap> fixturemaps = mParserFixtures.getInflatedMapsList();
			List<ArrayList<AutocastMap>> fixturecustoms = mParserFixtures.getInflatedCustomPropertiesList();
			int fixturecount = (fixturedefs != null) ? fixturedefs.size() : 0;
			for (int j = 0; j < fixturecount; j++) {
				Fixture f = mFixtureFactory.produce(fixturedefs.get(j), b);
				rubeDef.registerFixture(f, fixturemaps.get(j).getString("name", ""));

				installCustomProps(rubeDef, f, fixturecustoms.get(j));
			}
		}

		/* joints - regular joints go first */
		for (int i = 0; i < jointcount; i++) {
			Joint j = mJointsFactory.produce(world, bodies, jointdefs.get(i), jointmaps.get(i));
			rubeDef.registerJoint(j, i, jointmaps.get(i).getString("name", ""));
		}
		/* joints - gearjoints go second (each gearjoint references two instances of regular joints) */
		for (int i = 0; i < jointcount; i++) {
			Joint j = mJointsFactory.produceGearJoint(world, bodies, joints, jointdefs.get(i), jointmaps.get(i));
			rubeDef.registerJoint(j, i, jointmaps.get(i).getString("name", ""));

			installCustomProps(rubeDef, joints.get(i), jointcustoms.get(i));
		}

		/* images - will be created with provided IEntityFactory */
		for (int i = 0; i < imagecount; i++) {
			ImageDef imageDef = imagedefs.get(i);
			int bodyIndex = imagemaps.get(i).getInt("body", -1);
			if (-1 != bodyIndex) {
				imageDef.body = bodies.get(bodyIndex);
			} else {
				imageDef.body = null;
			}

			IEntity e = this.getEntityFactory().produce(world, imageDef, imagemaps.get(i));
			rubeDef.registerEntity(e, i, imagemaps.get(i).getString("name", ""), imageDef);

			if (imageDef.body != null) {
				rubeDef.mapEntityToBody(e, imageDef.body);
			}

			installCustomProps(rubeDef, images.get(i), imagecustoms.get(i));
		}

		return rubeDef;
	}

	protected PhysicsWorld createWorld(final Vector2 pGravity, int sim_positionIterations, int sim_velocityIterations, boolean sim_allowSleep) {
		return new PhysicsWorld(pGravity, sim_allowSleep, sim_positionIterations, sim_velocityIterations);
	}

	private void installCustomProps(RubeDef pRube, Object item, List<AutocastMap> pMap) {
		if (pMap != null) {
			int customcount = pMap.size();
			for (int i = 0; i < customcount; i++) {
				AutocastMap propValue = pMap.get(i);
				String propertyName = propValue.getString("name");
				if (propValue.has("int")) {
					pRube.setCustomInt(item, propertyName, propValue.getInt("int"));
				} else if (propValue.has("float")) {
					pRube.setCustomFloat(item, propertyName, (float) propValue.getFloat("float"));
				} else if (propValue.has("string")) {
					pRube.setCustomString(item, propertyName, propValue.getString("string"));
				} else if (propValue.has("vec2")) {
					pRube.setCustomVector(item, propertyName, propValue.getVector2("vec2"));
				} else if (propValue.has("bool")) {
					pRube.setCustomBool(item, propertyName, propValue.getBool("bool"));
				} else if (propValue.has("color")) {
					Color colorTint = new Color(1, 1, 1);
					List<Object> listRGBA = propValue.getList("color");
					int r = (Integer) listRGBA.get(0);
					int g = (Integer) listRGBA.get(1);
					int b = (Integer) listRGBA.get(2);
					int a = (Integer) listRGBA.get(3);
					colorTint.set(r/255f, g/255f, b/255f, a/255f);
					pRube.setCustomColor(item, propertyName, colorTint);
				}
			}
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================


	private static class BasicListener<T> implements IInflatingListener<T> {
		@Override
		public void onParsed(AdapterListToParser<T> parser, T result, AutocastMap map) {
			//System.out.println("parsed " + result + " from " + map);
		}

		@Override
		public void onParsingStarted(AdapterListToParser<T> parser) {
			//System.out.println("========== parsing " + parser.getKeyToInflate() + "... ==========");
		}

		@Override
		public void onParsingFinished(AdapterListToParser<T> parser) {
			//System.out.println("========== ... parsing " + parser.getKeyToInflate() + " ==========");
		}
	}
}