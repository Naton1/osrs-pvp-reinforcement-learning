from setuptools import find_packages, setup

setup(
    name="pvp-ml",
    version="1.0",
    packages=find_packages(),
    entry_points={
        # Alias frequently used commands to make easier
        "console_scripts": [
            "train = pvp_ml.run_train_job:main_entry_point",
            "serve-api = pvp_ml.api:main_entry_point",
            "eval = pvp_ml.evaluate:main_entry_point",
        ],
    },
)
