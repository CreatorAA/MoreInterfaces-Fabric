package online.pigeonshouse.moreinterfaces.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import java.util.Optional;
import java.util.function.Predicate;

public class WorldUtil {
    public static String getLevelName(Level level) {
        if (level == null) {
            return null;
        }

        return level.dimension().location().toString();
    }

    /**
     * 获取服务器上的世界（为了通用性，故意这样写的）
     *
     * @param level     任意有效世界
     * @param levelName 要查找的世界名称
     * @return 世界
     */
    public static ServerLevel getLevel(Level level, String levelName) {
        if (levelName.isEmpty() || level.getServer() == null) return null;

        Iterable<ServerLevel> allLevels = level.getServer().getAllLevels();

        for (ServerLevel serverLevel : allLevels) {
            if (serverLevel.dimension().location().toString().equals(levelName)) {
                return serverLevel;
            }
        }

        return null;
    }


    public static HitResult rayTrace(Entity entity, double d, boolean bl) {
        Vec3 vec3 = entity.getEyePosition(0);
        Vec3 vec32 = entity.getViewVector(0);
        Vec3 vec33 = vec3.add(vec32.x * d, vec32.y * d, vec32.z * d);

        HitResult result = entity.level().clip(new ClipContext(vec3, vec33,
                ClipContext.Block.OUTLINE, bl ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                entity));

        double maxSqDist = d * d;
        if (result.getType() == HitResult.Type.MISS) {
            maxSqDist = result.getLocation().distanceToSqr(vec3);
        }

        EntityHitResult hitResult =
                getEntityHitResult(entity.level(), entity, vec3, vec33,
                        new AABB(vec3, vec33).inflate(maxSqDist),
                        e -> !e.isSpectator() && e.isPickable());

        return hitResult != null ? hitResult : result;
    }

    /**
     * 根据给定的参数计算并返回一个碰撞结果。
     *
     * @param level 世界
     * @param x     起始点的X坐标
     * @param y     起始点的Y坐标
     * @param z     起始点的Z坐标
     * @param yaw   水平方向的旋转角度（以度为单位）
     * @param pitch 垂直方向的旋转角度（以度为单位）
     * @param d     射线的长度
     * @param bl    是否考虑流体（true 表示考虑，false 表示不考虑）
     * @return 碰撞结果对象
     */
    public static HitResult rayTrace(Level level, double x, double y, double z, float yaw, float pitch, double d, boolean bl) {
        Vec3 start = new Vec3(x, y, z);
        Vec3 direction = Vec3.directionFromRotation(pitch, yaw).scale(d);
        Vec3 end = start.add(direction);

        ClipContext context = new ClipContext(start, end,
                ClipContext.Block.OUTLINE,
                bl ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                null);

        BlockHitResult clip = level.clip(context);

        double maxSqDist = d * d;
        if (clip.getType() == HitResult.Type.MISS) {
            maxSqDist = clip.getLocation().distanceToSqr(start);
        }

        EntityHitResult hitResult =
                getEntityHitResult(level, null, start, end, new AABB(start, end).inflate(maxSqDist), entity -> !entity.isSpectator() && entity.isPickable());

        return hitResult != null ? hitResult : clip;
    }

    public static EntityHitResult getEntityHitResult(Level level, Entity entity, Vec3 vec3, Vec3 vec32, AABB aABB, Predicate<Entity> predicate) {
        double d = Double.MAX_VALUE;
        Entity entity2 = null;

        for (Entity entity3 : level.getEntities(entity, aABB, predicate)) {
            AABB aABB2 = entity3.getBoundingBox().inflate(entity3.getPickRadius());
            Optional<Vec3> optional = aABB2.clip(vec3, vec32);
            if (optional.isPresent()) {
                double e = vec3.distanceToSqr(optional.get());
                if (e < d) {
                    entity2 = entity3;
                    d = e;
                }
            }
        }

        return entity2 == null ? null : new EntityHitResult(entity2);
    }
}
