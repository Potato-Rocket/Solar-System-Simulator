q# Spaceflight-Simulator

A Java application to simulate 3D gravitational physics. Currently, this project is in early stages of development. Various integration methods may be implemented via the `Integrator` class, such as:

- The explicit Euler method
- The symplectic Euler method
- The velocity Verlet method

The orthographic 3D visuals are built on top of the `Graphics2D` library, and thus remain rather unrefined. Future improvements relating to parallel simulation step processing are planned.

## Configuration Setup

This program allows both the graphical parameters and the initial conditions of the simulation to be specified in user configuration files.

### Directory Structure

The simulator requires configuration files to be placed in a specific location:

1. Create a configuration directory:
   - **Linux/macOS**: Create a folder named `solarsystemsim` inside your `~/.config/` directory:
     ```
     mkdir -p ~/.config/solarsystemsim
     ```
   - **Windows**: Create a folder named `solarsystemsim` inside your user home directory's `.config` folder:
     ```
     mkdir %USERPROFILE%\.config\solarsystemsim
     ```

### Automatic Configuration Setup

For your convenience, we provide scripts to automatically set up the configuration directory:

- **Linux/macOS**: Run the `autoconfig.sh` script:
  ```
  ./autoconfig.sh
  ```
- **Windows**: Run the `autoconfig.bat` script:
  ```
  autoconfig.bat
  ```

These scripts will create the necessary directory structure and copy the example configuration files for you.

### Required Configuration Files

You need two main configuration files:

1. **setup.properties**: Contains general simulator settings
2. **system.properties**: (or any other name specified in setup.properties) Defines celestial bodies and their properties

### Setting Up Your Configuration

1. Copy the example configuration files from the project directory:
   ```
   cp example_setup.properties ~/.config/solarsystemsim/setup.properties
   cp example_system.properties ~/.config/solarsystemsim/system.properties
   ```
2. Edit these files to customize the simulation. Note that the simulation assumes that a consistent set of units is used throughout the system setup file. For example, if using kg-m-s, be sure to give every value in terms of these.

### Generating Configuration Files

You can also use our Python script to automatically generate system configuration files:

- Located in `resources/autogen.py`
- This script can generate configuration files with various celestial systems
- Run the script and follow the prompts to create custom system configurations

## Usage

When you start the program, the view will automatically encompass every one of your bodies. To rotate the view, click and drag on the screen. To zoom in or out, you can use the scroll wheel. Alternatively, these keybindings can be used:

- W: Tilt view up
- S: Tilt view down
- A: Rotate view left
- D: Rotate view right


- J: Zoom in
- K: Zoom out

To increase or decrease the virtual time-scale, use the following keybindings:

- Q: Slow down time
- E: Speed up time

To toggle the center of the view between different bodies, use these keybindings:

- H: Go to previous body
- L: Go to next body

Finally, these keybinding can be used for other functions:

- F1: Reset viewing angle and zoom
- F2: Toggle whether to view planet sizes to scale with distance or on a log scale
- F3: Toggle whether to show extra info on screen
- Escape: Close the application

## Screenshots

Here's what the simulator looks like in action:

![Solar System Simulator Screenshot 1](resources/Screenshot%202025-04-27%20104818.png)

![Solar System Simulator Screenshot 2](resources/Screenshot%202025-04-27%20104930.png)
