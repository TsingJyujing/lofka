import os

from setuptools import setup, find_packages

here = os.path.abspath(os.path.dirname(__file__))

try:
    README = open(os.path.join(here, 'README.md'), encoding='utf-8').read()
except:
    README = ""

setup(
    name="lofka-tail",
    version=0.1,
    author="Tsing Jyujing",
    author_email="tsingjyujing@163.com",
    url="https://github.com/TsingJyujing/lofka",
    description="Monitoring structured file",
    install_requires=["requests"],
    long_description=README,
    packages=find_packages(),
    platforms='any',
    zip_safe=True,
    include_package_data=True,
    entry_points={
        'console_scripts': [
            'lofka-tail=lofka.tail:entry',
        ],
    }
)
