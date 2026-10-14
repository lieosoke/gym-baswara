import json

data = """
A. CHEST (Dada)
Barbell Flat Bench Press	Barbell	Pectoralis Major (Mid)
Barbell Incline Bench Press	Barbell	Pectoralis Major (Upper)
Barbell Decline Bench Press	Barbell	Pectoralis Major (Lower)
Dumbbell Flat Press	Dumbbell	Pectoralis Major
Dumbbell Incline Press	Dumbbell	Pectoralis Major (Upper)
Dumbbell Fly	Dumbbell	Pectoralis Major (Isolation)
Machine Chest Press	Leverage Machine	Pectoralis Major
Pec Deck Machine	Leverage Machine	Pectoralis Major
Cable Crossover	Cable	Pectoralis Major
Cable Fly	Cable	Pectoralis Major
Push-Up	Body Weight	Pectoralis Major, Triceps
Dip	Body Weight	Pectoralis Major (Lower)
Weighted Dip	Body Weight	Pectoralis Major (Lower)

B. BACK (Punggung)
Barbell Bent-Over Row	Barbell	Latissimus Dorsi, Rhomboids
Barbell Deadlift	Barbell	Erector Spinae, Latissimus Dorsi
Barbell Pendlay Row	Barbell	Upper Back
Dumbbell One-Arm Row	Dumbbell	Latissimus Dorsi
Dumbbell Bent-Over Row	Dumbbell	Middle Back
Seated Cable Row	Cable	Middle Back
Lat Pulldown	Cable	Latissimus Dorsi
Pull-Up	Body Weight	Latissimus Dorsi
Chin-Up	Body Weight	Latissimus Dorsi, Biceps
Inverted Row	Body Weight	Upper Back
Lever Seated Row	Leverage Machine	Latissimus Dorsi
Assisted Chin-Up	Leverage Machine	Latissimus Dorsi

C. SHOULDERS (Bahu)
Barbell Overhead Press	Barbell	Deltoid (Anterior)
Barbell Upright Row	Barbell	Deltoid (Lateral)
Dumbbell Shoulder Press	Dumbbell	Deltoid (Anterior)
Dumbbell Lateral Raise	Dumbbell	Deltoid (Lateral)
Dumbbell Front Raise	Dumbbell	Deltoid (Anterior)
Arnold Press	Dumbbell	Deltoid (All Heads)
Cable Lateral Raise	Cable	Deltoid (Lateral)
Face Pull	Cable	Rear Deltoid
Machine Shoulder Press	Leverage Machine	Deltoid (Anterior)
Pike Push-Up	Body Weight	Deltoid (Anterior)
Handstand Push-Up	Body Weight	Deltoid (All Heads)

D. BICEPS
Barbell Curl	Barbell	Biceps Brachii
Preacher Curl	Barbell	Biceps Brachii (Short Head)
Dumbbell Curl	Dumbbell	Biceps Brachii
Hammer Curl	Dumbbell	Brachialis, Biceps
Incline Dumbbell Curl	Dumbbell	Biceps Brachii (Long Head)
Cable Bicep Curl	Cable	Biceps Brachii
Cable Hammer Curl	Cable	Brachialis
Chin-Up	Body Weight	Biceps Brachii
Machine Bicep Curl	Leverage Machine	Biceps Brachii

E. TRICEPS
Close Grip Bench Press	Barbell	Triceps Brachii
Skull Crusher	Barbell	Triceps Brachii (Long Head)
Dumbbell Overhead Extension	Dumbbell	Triceps Brachii (Long Head)
Dumbbell Kickback	Dumbbell	Triceps Brachii (Lateral Head)
Cable Tricep Pushdown	Cable	Triceps Brachii
Cable Overhead Extension	Cable	Triceps Brachii (Long Head)
Bench Dip	Body Weight	Triceps Brachii
Tricep Dip	Body Weight	Triceps Brachii
Machine Tricep Extension	Leverage Machine	Triceps Brachii

F. LEGS (Kaki)
Barbell Back Squat	Barbell	Quadriceps, Glutes
Barbell Front Squat	Barbell	Quadriceps
Barbell Romanian Deadlift	Barbell	Hamstrings, Glutes
Barbell Lunge	Barbell	Quadriceps, Glutes
Dumbbell Goblet Squat	Dumbbell	Quadriceps
Dumbbell Lunges	Dumbbell	Quadriceps, Glutes
Dumbbell Bulgarian Split Squat	Dumbbell	Quadriceps, Glutes
Leg Press	Leverage Machine	Quadriceps
Leg Extension	Leverage Machine	Quadriceps
Leg Curl	Leverage Machine	Hamstrings
Hack Squat	Leverage Machine	Quadriceps
Cable Leg Extension	Cable	Quadriceps
Bodyweight Squat	Body Weight	Quadriceps
Walking Lunge	Body Weight	Quadriceps, Glutes
Glute Bridge	Body Weight	Glutes
Calf Raise	Body Weight	Calves

G. CORE (Perut)
Ab Crunch Machine	Leverage Machine	Rectus Abdominis
Cable Crunch	Cable	Rectus Abdominis
Plank	Body Weight	Rectus Abdominis
Russian Twist	Body Weight	Obliques
Hanging Leg Raise	Body Weight	Lower Abs
Sit-Up	Body Weight	Rectus Abdominis
Ab Roller	Other	Rectus Abdominis
"""

exercises = []
for line in data.strip().split('\n'):
    line = line.strip()
    if not line or line.startswith(('A.', 'B.', 'C.', 'D.', 'E.', 'F.', 'G.')):
        continue
    
    parts = line.split('\t')
    if len(parts) == 3:
        name = parts[0].strip()
        equipment = parts[1].strip()
        primaryMuscle = parts[2].strip()
        exercises.append({
            "name": name,
            "primaryMuscle": primaryMuscle,
            "equipment": equipment
        })

with open(r'd:\gymbaswara\app\app\src\main\assets\exercises.json', 'w') as f:
    json.dump(exercises, f, indent=2)

print(f"Generated {len(exercises)} exercises.")
