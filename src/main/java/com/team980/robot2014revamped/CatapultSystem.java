/*
 *  MIT License
 *
 *  Copyright (c) 2018-2019 FRC Team 980 ThunderBots
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.team980.robot2014revamped;

import edu.wpi.first.wpilibj.*;

import static com.team980.robot2014revamped.Parameters.*;

class CatapultSystem {

    private Talon winchMotor;

    private Solenoid ratchetSolenoid;

    private DigitalInput leftLockSwitch;
    private DigitalInput rightLockSwitch;

    //private Timer lockingTimer;

    private CatapultState state = CatapultState.INITIALIZED;

    public CatapultSystem() {
        winchMotor = new Talon(WINCH_MOTOR_PWM_CHANNEL);
        winchMotor.setName("Catapult System", "Winch Motor");

        ratchetSolenoid = new Solenoid(PCM_CAN_ID, RATCHET_SOLENOID_CHANNEL);
        ratchetSolenoid.setName("Catapult System", "Ratchet Solenoid");

        leftLockSwitch = new DigitalInput(LEFT_LOCK_SWITCH_DIO_CHANNEL);
        leftLockSwitch.setName("Catapult System", "Left Limit Switch");
        rightLockSwitch = new DigitalInput(RIGHT_LOCK_SWITCH_DIO_CHANNEL);
        rightLockSwitch.setName("Catapult System", "Right Limit Switch");

        //lockingTimer = new Timer();
    }

    public CatapultState getState() {
        return state;
    }

    public void init() {
        state = CatapultState.INITIALIZED;
        ratchetSolenoid.set(RATCHET_LOCKED_VALUE);
    }

    public void operate(XboxController controller, Robot.RollerState rollerState) {
        if (state == CatapultState.LOADING && rollerState == Robot.RollerState.EXTENDED) {
            if (!leftLockSwitch.get() || !rightLockSwitch.get()) {
                winchMotor.set(0);
                state = CatapultState.LOCKED;
            } else {
                winchMotor.set(WINCH_MOTOR_SPEED);
            }
        }  else {
            winchMotor.set(0);
        }

        if (controller.getTriggerAxis(GenericHID.Hand.kLeft) > TRIGGER_THRESHOLD) { //load catapult
            ratchetSolenoid.set(RATCHET_LOCKED_VALUE);
            state = CatapultState.LOADING;
        }

        if (controller.getTriggerAxis(GenericHID.Hand.kRight) > TRIGGER_THRESHOLD
                && rollerState == Robot.RollerState.EXTENDED) { //unlock ratchet
            ratchetSolenoid.set(RATCHET_UNLOCKED_VALUE);
            state = CatapultState.FIRED;
        }
    }

    public void disable() {
        winchMotor.disable();
    }

    public enum CatapultState {
        INITIALIZED,
        LOADING,
        LOCKED,
        FIRED
    }
}
