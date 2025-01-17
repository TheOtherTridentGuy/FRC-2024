package frc.robot.commands;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import frc.robot.Constants;
import frc.robot.Constants.SpinState;
import frc.robot.subsystems.Deflector;
import frc.robot.subsystems.Kinesthetics;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.Swerve;
import frc.lib.math.RobotState;

public class AmpAuto extends ParallelCommandGroup {
    public AmpAuto(Kinesthetics k, Swerve sw, Shooter sh, Deflector d) {
        var desiredPose = Constants.Environment.amps.get(k.getAlliance());
        // offset the desired pose so the front of the robot touches the amp, not the center (which would be bad)
        desiredPose.plus(new Transform2d(0, -Constants.Swerve.trackWidth/2 - Constants.CommandConstants.bumperWidth, new Rotation2d()));
        addCommands(
            new SwerveAuto(k, sw, new RobotState(desiredPose)),
            new SequentialCommandGroup(
                new ParallelCommandGroup(
                    sh.toPitch(Constants.CommandConstants.ampShooterCommand.pitch()),
                    new WaitUntilCommand(() ->
                        k.getPose().minus(desiredPose).getTranslation().getNorm() < Constants.CommandConstants.ampAutoDistanceToStartSpinning
                    )
                ),
                sh.toSpin(
                    Constants.CommandConstants.ampShooterCommand.lSpin(),
                    Constants.CommandConstants.ampShooterCommand.rSpin()
                ),
                new ParallelDeadlineGroup(
                    sh.new ChangeNeck(k, SpinState.FW),
                    d.new Raise()
                ),
                sh.stopShooter()
            )
        );
    }

    public static boolean isInRange(Kinesthetics k) { // are we close enough
        return 
            Constants.Environment.amps.get(k.getAlliance()).getTranslation()
            .getDistance(k.getPose().getTranslation())
            < Constants.CommandConstants.ampAutoDistanceMax;
    }
}
