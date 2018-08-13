# coding=utf-8
from setuptools import find_packages, setup

setup(
    name="lofka",
    version="1.7",
    packages=find_packages(),
    install_requires=[
        "requests"
    ],
    package_data={
        # If any package contains *.txt or *.rst files, include them:
        '': ["*.md", "*.yml", "*.rst", "*.json"],
    },
    # metadata for upload to PyPI
    author="yuanyifan",
    author_email="tsingjyujing@163.com",
    description="Lofka logging http handler",
    license="LGPL",
    keywords="lofka logging"
)
